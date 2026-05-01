package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    public String chatWithAI(String userMessage) {
        User currentUser = getCurrentUser();
        List<Task> userTasks = taskRepository.findByUser(currentUser);

        String context = buildContextFromTasks(userTasks);

        String prompt = buildPrompt(context, userMessage);

        return callDeepSeekAPI(prompt);
    }

    private String callDeepSeekAPI(String prompt) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content", "Ты умный ассистент по управлению задачами. Ты помогаешь пользователю планировать, напоминаешь о дедлайнах, даёшь советы по приоритетам. Отвечай кратко и по делу, на русском языке."),
                    Map.of("role", "user", "content", prompt)
            });
            requestBody.put("stream", false);

            String responseJson = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            return "Извините, произошла ошибка: " + e.getMessage();
        }
    }

    private String buildContextFromTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "У пользователя пока нет задач.";
        }

        StringBuilder context = new StringBuilder("Задачи пользователя:\n");
        for (Task task : tasks) {
            context.append(String.format("- %s (приоритет: %s, статус: %s, дедлайн: %s)\n",
                    task.getTitle(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline() != null ? task.getDeadline() : "не указан"));
        }
        return context.toString();
    }

    private String buildPrompt(String context, String userMessage) {
        return context + "\n\nВопрос пользователя: " + userMessage +
                "\n\nОтветь на вопрос пользователя, используя контекст его задач. Если вопрос не связан с задачами, просто ответь как полезный ассистент.";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}