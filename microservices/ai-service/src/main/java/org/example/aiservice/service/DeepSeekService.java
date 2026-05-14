package org.example.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public synchronized void updateConfig(String apiUrl, String model, String apiKey) {
        if (apiUrl != null && !apiUrl.isBlank()) {
            this.apiUrl = apiUrl.trim();
        }
        if (model != null && !model.isBlank()) {
            this.model = model.trim();
        }
        if (apiKey != null && !apiKey.isBlank()) {
            this.apiKey = apiKey.trim();
        }
    }

    public synchronized void setApiUrl(String apiUrl) {
        this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl").trim();
    }

    public synchronized void setModel(String model) {
        this.model = Objects.requireNonNull(model, "model").trim();
    }

    public synchronized void setApiKey(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey").trim();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }

    public String getApiKey() {
        return apiKey;
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