package org.example.analyticsservice.service;

import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.entity.TaskStatsEntity;
import org.example.analyticsservice.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    @Transactional(readOnly = true)
    public TaskStatsDto getTaskStats() {
        var statsList = analyticsRepository.findAll();

        if (statsList.isEmpty()) {
            return getEmptyStats();
        }

        long totalTasks = 0;
        long completedTasks = 0;
        long pendingTasks = 0;
        long overdueTasks = 0;
        long highPriorityTasks = 0;
        long mediumPriorityTasks = 0;
        long lowPriorityTasks = 0;

        for (TaskStatsEntity entity : statsList) {
            totalTasks += entity.getTotalTasks();
            completedTasks += entity.getCompletedTasks();
            pendingTasks += entity.getPendingTasks();
            overdueTasks += entity.getOverdueTasks();
            highPriorityTasks += entity.getHighPriorityTasks();
            mediumPriorityTasks += entity.getMediumPriorityTasks();
            lowPriorityTasks += entity.getLowPriorityTasks();
        }

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        return new TaskStatsDto(
                totalTasks,
                completedTasks,
                pendingTasks,
                overdueTasks,
                completionRate,
                highPriorityTasks,
                mediumPriorityTasks,
                lowPriorityTasks
        );
    }

    @Transactional(readOnly = true)
    public TaskStatsDto getUserTaskStats(Long userId) {
        return analyticsRepository.findByUserId(userId)
                .map(this::convertToDto)
                .orElseGet(this::getEmptyStats);
    }

    @Transactional
    public void saveOrUpdateStats(Long userId, TaskStatsDto stats) {
        TaskStatsEntity entity = convertToEntity(userId, stats);

        analyticsRepository.findByUserId(userId)
                .ifPresent(existing -> entity.setId(existing.getId()));

        analyticsRepository.save(entity);
    }

    @Transactional
    public void deleteUserStats(Long userId) {
        analyticsRepository.deleteByUserId(userId);
    }

    private TaskStatsDto convertToDto(TaskStatsEntity entity) {
        return new TaskStatsDto(
                entity.getTotalTasks(),
                entity.getCompletedTasks(),
                entity.getPendingTasks(),
                entity.getOverdueTasks(),
                entity.getCompletionRate(),
                entity.getHighPriorityTasks(),
                entity.getMediumPriorityTasks(),
                entity.getLowPriorityTasks()
        );
    }

    private TaskStatsEntity convertToEntity(Long userId, TaskStatsDto dto) {
        TaskStatsEntity entity = new TaskStatsEntity();
        entity.setUserId(userId);
        entity.setTotalTasks(dto.getTotalTasks());
        entity.setCompletedTasks(dto.getCompletedTasks());
        entity.setPendingTasks(dto.getPendingTasks());
        entity.setOverdueTasks(dto.getOverdueTasks());
        entity.setCompletionRate(dto.getCompletionRate());
        entity.setHighPriorityTasks(dto.getHighPriorityTasks());
        entity.setMediumPriorityTasks(dto.getMediumPriorityTasks());
        entity.setLowPriorityTasks(dto.getLowPriorityTasks());
        entity.setLastUpdated(LocalDateTime.now());
        return entity;
    }

    private TaskStatsDto getEmptyStats() {
        return new TaskStatsDto(0, 0, 0, 0, 0.0, 0, 0, 0);
    }
}