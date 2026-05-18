package org.example.taskservice.service;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.mapper.TaskMapper;
import org.example.taskservice.repository.TagRepository;
import org.example.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TagRepository tagRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private TaskMapper taskMapper;
    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUpMapper() {
        lenient().when(taskMapper.toResponse(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getDeadline(),
                    task.getUserId()
            );
        });
    }

    @Test
    void createTask_shouldSaveAndReturnResponse() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Test Task");
        request.setDescription("A".repeat(1200));
        request.setPriority("HIGH");
        Long userId = 1L;

        Task savedTask = new Task();
        savedTask.setId(100L);
        savedTask.setTitle("Test Task");
        savedTask.setDescription(request.getDescription());
        savedTask.setPriority("HIGH");
        savedTask.setUserId(userId);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request, userId);

        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getDescription()).hasSize(1200);
        assertThat(response.getUserId()).isEqualTo(userId);
        verify(taskRepository).save(any(Task.class));
        verify(restTemplate).postForEntity(anyString(), any(), eq(Void.class));
    }

    @Test
    void getUserTasks_shouldReturnList() {
        Long userId = 1L;
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task");
        task.setUserId(userId);
        when(taskRepository.findByUserId(userId)).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getUserTasks(userId);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task");
    }

    @Test
    void updateTask_shouldUpdateWhenOwned() {
        Long taskId = 1L;
        Long userId = 1L;
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated");
        request.setPriority("LOW");

        Task existing = new Task();
        existing.setId(taskId);
        existing.setUserId(userId);
        existing.setTitle("Old");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenReturn(existing);

        TaskResponse response = taskService.updateTask(taskId, request, userId);

        assertThat(response.getTitle()).isEqualTo("Updated");
        verify(taskRepository).save(existing);
    }

    @Test
    void deleteTask_shouldDeleteWhenOwned() {
        Long taskId = 1L;
        Long userId = 1L;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId, userId);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_shouldThrowWhenNotOwned() {
        Long taskId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(otherUserId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.deleteTask(taskId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("don't own");
        verify(taskRepository, never()).delete(any());
    }
}
