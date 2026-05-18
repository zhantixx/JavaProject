package org.example.aiservice.controller;

import org.example.aiservice.dto.RecommendationRequest;
import org.example.aiservice.dto.RecommendationResponse;
import org.example.aiservice.service.DeepSeekService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/recommendations")
public class RecommendationController {

    private final DeepSeekService deepSeekService;

    public RecommendationController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping
    public RecommendationResponse recommend(@RequestBody RecommendationRequest request) {
        return deepSeekService.buildRecommendations(request);
    }
}
