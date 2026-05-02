package org.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.taskservice.entity.Task;
import org.example.entity.User;
import org.example.taskservice.repository.TaskRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    @Cacheable(value = "aiResponses", key = "#userMessage + '_' + getCurrentUser().getEmail()", unless = "#result == null || #result.length() > 500")
    public String chatWithAI(String userMessage) {
        System.out.println("🤖 Вызов DeepSeek API (не из кэша): " + userMessage);

        User currentUser = getCurrentUser();
        List<Task> userTasks = taskRepository.findByUser(currentUser);

        String context = buildContextFromTasks(userTasks);
        String prompt = buildPrompt(context, userMessage);

        return callDeepSeekAPI(prompt);
    }

    private String getUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
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
            requestBody.put("temperature", 0.7);  // Добавим параметры генерации
            requestBody.put("max_tokens", 500);   // Ограничим длину ответа

            long startTime = System.currentTimeMillis();
            String responseJson = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            long endTime = System.currentTimeMillis();

            System.out.println("⏱️ DeepSeek API ответил за " + (endTime - startTime) + "ms");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);
            String reply = root.path("choices").get(0).path("message").path("content").asText();

            // Проверка на использование токенов (если вернёт API)
            if (root.has("usage")) {
                int totalTokens = root.path("usage").path("total_tokens").asInt();
                System.out.println("📊 Использовано токенов: " + totalTokens);
            }

            return reply;

        } catch (Exception e) {
            System.err.println("❌ Ошибка DeepSeek API: " + e.getMessage());
            return "Извините, произошла ошибка: " + e.getMessage();
        }
    }

    public String chatWithAIWithoutCache(String userMessage) {
        System.out.println("🔄 Принудительный вызов API (без кэша)");
        User currentUser = getCurrentUser();
        List<Task> userTasks = taskRepository.findByUser(currentUser);
        String context = buildContextFromTasks(userTasks);
        String prompt = buildPrompt(context, userMessage);
        return callDeepSeekAPI(prompt);
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
                "\n\nОтветь на вопрос пользователя, используя контекст его задач. Если вопрос не связан с задачами, просто ответь как полезный ассистент. Будь конкретным и полезным.";
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}