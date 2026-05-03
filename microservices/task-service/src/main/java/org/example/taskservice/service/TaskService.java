package org.example.taskservice.service;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(TaskRequest request, Long userId) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        task.setDeadline(request.getDeadline());
        task.setUserId(userId);

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    public List<TaskResponse> getUserTasks(Long userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long id, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDeadline(request.getDeadline());

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getUserId()
        );
    }
}