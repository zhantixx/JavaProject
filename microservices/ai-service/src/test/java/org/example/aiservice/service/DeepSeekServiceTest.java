package org.example.aiservice.service;

import org.example.aiservice.dto.RecommendationRequest;
import org.example.aiservice.dto.RecommendationTaskInput;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekServiceTest {

    @Test
    void updateConfig_shouldReplaceOnlyProvidedValues() {
        DeepSeekService service = new DeepSeekService(WebClient.builder());
        service.setApiUrl("https://old.example/api");
        service.setModel("old-model");
        service.setApiKey("old-key");

        service.updateConfig(" https://new.example/api ", "new-model", "");

        assertThat(service.getApiUrl()).isEqualTo("https://new.example/api");
        assertThat(service.getModel()).isEqualTo("new-model");
        assertThat(service.getApiKey()).isEqualTo("old-key");
    }

    @Test
    void buildRecommendations_shouldReturnFallbackRecommendationsWhenApiKeyMissing() {
        DeepSeekService service = new DeepSeekService(WebClient.builder());
        service.setApiUrl("https://example.com/api");
        service.setModel("test-model");
        service.setApiKey("");

        RecommendationTaskInput task = new RecommendationTaskInput();
        task.setTitle("Диплом презентациясын аяқтау");
        task.setPriority("HIGH");
        task.setStatus("PLANNED");
        task.setDeadline("2026-05-01T10:00:00Z");

        RecommendationRequest request = new RecommendationRequest();
        request.setUserName("Жанат");
        request.setTasks(java.util.List.of(task));

        var response = service.buildRecommendations(request);

        assertThat(response.getMode()).isEqualTo("local");
        assertThat(response.getSummary()).contains("Жанат");
        assertThat(response.getRecommendations()).hasSize(3);
    }
}
