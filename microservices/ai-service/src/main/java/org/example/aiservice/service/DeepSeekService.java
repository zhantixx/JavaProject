package org.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeepSeekService {

    private final WebClient.Builder webClientBuilder;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    public DeepSeekService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public String chatWithAI(String userMessage) {
        return callDeepSeekAPI(userMessage);
    }

    private String callDeepSeekAPI(String prompt) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", new Object[]{
                    Map.of("role", "system", "content", "Ты умный ассистент. Отвечай кратко и по делу, на русском языке."),
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
}