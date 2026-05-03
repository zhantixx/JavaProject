package org.example.analyticsservice.service;

import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.entity.TaskStatsEntity;
import org.example.analyticsservice.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public TaskStatsDto getTaskStats() {
        List<TaskStatsEntity> list = repository.findAll();

        long totalTasks = 0, completedTasks = 0, pendingTasks = 0, overdueTasks = 0;
        long high = 0, medium = 0, low = 0;
        double rate = 0;

        for (TaskStatsEntity e : list) {
            totalTasks += e.getTotalTasks();
            completedTasks += e.getCompletedTasks();
            pendingTasks += e.getPendingTasks();
            overdueTasks += e.getOverdueTasks();
            high += e.getHighPriorityTasks();
            medium += e.getMediumPriorityTasks();
            low += e.getLowPriorityTasks();
        }

        if (totalTasks > 0) rate = (double) completedTasks / totalTasks * 100;

        return new TaskStatsDto(totalTasks, completedTasks, pendingTasks, overdueTasks, rate, high, medium, low);
    }

    public TaskStatsDto getUserTaskStats(Long userId) {
        TaskStatsEntity entity = repository.findByUserId(userId).orElse(null);
        if (entity == null) return new TaskStatsDto(0, 0, 0, 0, 0, 0, 0, 0);
        return new TaskStatsDto(
                entity.getTotalTasks(), entity.getCompletedTasks(), entity.getPendingTasks(),
                entity.getOverdueTasks(), entity.getCompletionRate(), entity.getHighPriorityTasks(),
                entity.getMediumPriorityTasks(), entity.getLowPriorityTasks()
        );
    }
}