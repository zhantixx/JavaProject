package org.example.analyticsservice.service;

import org.example.analyticsservice.dto.TaskDto;
import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.entity.TaskStatsEntity;
import org.example.analyticsservice.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;
    private final RestTemplate restTemplate;

    @Value("${task.service.url:http://localhost:8082}")
    private String taskServiceUrl;

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    public AnalyticsService(AnalyticsRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    /**
     * Глобальная агрегированная статистика по сохранённым данным.
     * (Для живых данных по пользователю используйте getUserTaskStats)
     */
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

    /**
     * Живая статистика: получает задачи пользователя из task-service и вычисляет метрики.
     * При неудаче запроса — возвращает сохранённые агрегаты как fallback.
     */
    public TaskStatsDto getUserTaskStats(Long userId) {
        try {
            String url = taskServiceUrl + "/api/tasks/user/" + userId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalApiSecret);
            ResponseEntity<TaskDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    TaskDto[].class
            );
            TaskDto[] tasks = response.getBody();
            List<TaskDto> list = tasks == null ? List.of() : Arrays.asList(tasks);
            TaskStatsDto computed = computeStats(list);

            // Сохраним агрегат как кэш
            upsertStats(userId, computed);
            return computed;
        } catch (Exception ex) {
            // Fallback на сохранённые агрегаты
            TaskStatsEntity entity = repository.findByUserId(userId).orElse(null);
            if (entity == null) return new TaskStatsDto(0, 0, 0, 0, 0, 0, 0, 0);
            return new TaskStatsDto(
                    entity.getTotalTasks(), entity.getCompletedTasks(), entity.getPendingTasks(),
                    entity.getOverdueTasks(), entity.getCompletionRate(), entity.getHighPriorityTasks(),
                    entity.getMediumPriorityTasks(), entity.getLowPriorityTasks()
            );
        }
    }

    private TaskStatsDto computeStats(List<TaskDto> tasks) {
        long total = tasks.size();
        long completed = 0;
        long canceled = 0;
        long overdue = 0;
        long high = 0, medium = 0, low = 0;

        LocalDateTime now = LocalDateTime.now();

        for (TaskDto t : tasks) {
            String status = safeUpper(t.getStatus());
            String pr = safeUpper(t.getPriority());

            if ("DONE".equals(status)) completed++;
            if ("CANCELED".equals(status)) canceled++;

            if (!"DONE".equals(status) && !"CANCELED".equals(status)
                    && t.getDeadline() != null && t.getDeadline().isBefore(now)) {
                overdue++;
            }

            if ("HIGH".equals(pr)) high++;
            else if ("MEDIUM".equals(pr)) medium++;
            else if ("LOW".equals(pr)) low++;
        }

        long pending = total - completed - canceled;
        if (pending < 0) pending = 0;

        double rate = total > 0 ? (double) completed / total * 100.0 : 0.0;

        return new TaskStatsDto(total, completed, pending, overdue, rate, high, medium, low);
    }

    private void upsertStats(Long userId, TaskStatsDto dto) {
        TaskStatsEntity e = repository.findByUserId(userId).orElseGet(TaskStatsEntity::new);
        e.setUserId(userId);
        e.setTotalTasks(dto.getTotalTasks());
        e.setCompletedTasks(dto.getCompletedTasks());
        e.setPendingTasks(dto.getPendingTasks());
        e.setOverdueTasks(dto.getOverdueTasks());
        e.setCompletionRate(dto.getCompletionRate());
        e.setHighPriorityTasks(dto.getHighPriorityTasks());
        e.setMediumPriorityTasks(dto.getMediumPriorityTasks());
        e.setLowPriorityTasks(dto.getLowPriorityTasks());
        e.setLastUpdated(LocalDateTime.now());
        repository.save(e);
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.toUpperCase(Locale.ROOT);
    }
}
