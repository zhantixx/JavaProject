package org.example.aiservice.controller;

import org.example.aiservice.service.DeepSeekService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/admin")
public class AdminAiController {

    private final DeepSeekService deepSeekService;

    public AdminAiController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @GetMapping("/config")
    public AiConfigResponse getConfig() {
        String apiUrl = deepSeekService.getApiUrl();
        String model = deepSeekService.getModel();
        String apiKey = deepSeekService.getApiKey();
        String maskedKey = maskKey(apiKey);
        return new AiConfigResponse(apiUrl, model, maskedKey);
    }

    @PutMapping("/config")
    public ResponseEntity<AiConfigResponse> updateConfig(@RequestBody AiConfigRequest request) {
        deepSeekService.updateConfig(request.apiUrl(), request.model(), request.apiKey());
        String maskedKey = maskKey(deepSeekService.getApiKey());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new AiConfigResponse(deepSeekService.getApiUrl(), deepSeekService.getModel(), maskedKey));
    }

    private String maskKey(String key) {
        if (key == null || key.isBlank()) return "";
        int keep = Math.min(4, key.length());
        String suffix = key.substring(key.length() - keep);
        return "****" + suffix;
    }

    public static record AiConfigRequest(String apiUrl, String model, String apiKey) {}
    public static record AiConfigResponse(String apiUrl, String model, String apiKeyMasked) {}
}
