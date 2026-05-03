package org.example.analyticsservice.controller;

import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public TaskStatsDto getStats() {
        return service.getTaskStats();
    }

    @GetMapping("/stats/{userId}")
    public TaskStatsDto getUserStats(@PathVariable Long userId) {
        return service.getUserTaskStats(userId);
    }
}