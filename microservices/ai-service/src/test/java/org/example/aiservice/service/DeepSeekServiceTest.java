package org.example.aiservice.service;

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
}
