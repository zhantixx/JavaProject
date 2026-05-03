package org.example.aiservice.controller;

import org.example.aiservice.dto.ChatRequest;
import org.example.aiservice.dto.ChatResponse;
import org.example.aiservice.service.DeepSeekService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final DeepSeekService deepSeekService;

    public ChatController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = deepSeekService.chatWithAI(request.getMessage());
        return new ChatResponse(reply);
    }
}