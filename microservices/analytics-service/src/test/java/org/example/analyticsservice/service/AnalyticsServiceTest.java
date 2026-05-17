package org.example.analyticsservice.service;

import org.example.analyticsservice.dto.TaskDto;
import org.example.analyticsservice.dto.TaskStatsDto;
import org.example.analyticsservice.entity.TaskStatsEntity;
import org.example.analyticsservice.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private AnalyticsRepository repository;
    @Mock private RestTemplate restTemplate;

    @Test
    void getUserTaskStats_shouldComputeLiveStatsAndCacheThem() {
        AnalyticsService service = new AnalyticsService(repository, restTemplate);
        ReflectionTestUtils.setField(service, "taskServiceUrl", "http://task-service:8082");

        TaskDto done = task("DONE", "HIGH", LocalDateTime.now().minusDays(1));
        TaskDto overdue = task("PLANNED", "LOW", LocalDateTime.now().minusHours(1));

        when(restTemplate.getForObject("http://task-service:8082/api/tasks/user/7", TaskDto[].class))
                .thenReturn(new TaskDto[]{done, overdue});
        when(repository.findByUserId(7L)).thenReturn(Optional.empty());
        when(repository.save(any(TaskStatsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskStatsDto stats = service.getUserTaskStats(7L);

        assertThat(stats.getTotalTasks()).isEqualTo(2);
        assertThat(stats.getCompletedTasks()).isEqualTo(1);
        assertThat(stats.getPendingTasks()).isEqualTo(1);
        assertThat(stats.getOverdueTasks()).isEqualTo(1);
        assertThat(stats.getCompletionRate()).isEqualTo(50.0);
        assertThat(stats.getHighPriorityTasks()).isEqualTo(1);
        assertThat(stats.getLowPriorityTasks()).isEqualTo(1);
        verify(repository).save(any(TaskStatsEntity.class));
    }

    private TaskDto task(String status, String priority, LocalDateTime deadline) {
        TaskDto dto = new TaskDto();
        dto.setStatus(status);
        dto.setPriority(priority);
        dto.setDeadline(deadline);
        return dto;
    }
}
