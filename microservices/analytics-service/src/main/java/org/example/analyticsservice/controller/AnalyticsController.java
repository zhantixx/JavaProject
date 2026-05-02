package org.example.analyticsservice.controller;

import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDto> getTaskStats() {
        return ResponseEntity.ok(analyticsService.getTaskStats());
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<TaskStatsDto> getUserTaskStats(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserTaskStats(userId));
    }
}