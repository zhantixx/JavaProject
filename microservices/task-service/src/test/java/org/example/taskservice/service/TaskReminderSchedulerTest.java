package org.example.taskservice.service;

import org.example.taskservice.dto.NotificationRequest;
import org.example.taskservice.dto.UserNotificationProfileResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskReminderSchedulerTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final TaskNotificationProducer notificationProducer = mock(TaskNotificationProducer.class);
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final TaskReminderScheduler scheduler = new TaskReminderScheduler(taskRepository, notificationProducer, restTemplate);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "userServiceUrl", "http://user-service:8081");
        ReflectionTestUtils.setField(scheduler, "reminderWindowMinutes", 60L);
        ReflectionTestUtils.setField(scheduler, "internalApiSecret", "secret");
    }

    @Test
    void publishDeadlineNotifications_shouldSendReminderAndMarkTask() {
        Task task = new Task();
        task.setId(10L);
        task.setUserId(1L);
        task.setTitle("Finish report");
        task.setStatus("PLANNED");
        task.setDeadline(LocalDateTime.now().plusMinutes(30));

        UserNotificationProfileResponse profile = new UserNotificationProfileResponse();
        profile.setEmail("user@example.com");
        profile.setEmailNotificationsEnabled(true);
        profile.setPushNotificationsEnabled(true);
        profile.setReminderWindowMinutes(60);

        when(taskRepository.findByDeadlineBetweenAndReminderSentFalse(any(), any())).thenReturn(List.of(task));
        when(taskRepository.findByDeadlineBeforeAndOverdueSentFalse(any())).thenReturn(List.of());
        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.anyString(),
                any(),
                any(),
                org.mockito.ArgumentMatchers.eq(UserNotificationProfileResponse.class)
        ))
                .thenReturn(ResponseEntity.ok(profile));

        scheduler.publishDeadlineNotifications();

        verify(notificationProducer, org.mockito.Mockito.times(2)).send(any(NotificationRequest.class));
        verify(taskRepository).save(task);
    }
}
