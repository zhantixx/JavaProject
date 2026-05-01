package org.example.controller;


import  org.example.dto.ChatRequest;
import  org.example.dto.ChatResponse;
import  org.example.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final DeepSeekService deepSeekService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String reply = deepSeekService.chatWithAI(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}