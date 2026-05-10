package org.example.taskservice.service;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    public TaskService(TaskRepository taskRepository, RestTemplate restTemplate) {
        this.taskRepository = taskRepository;
        this.restTemplate = restTemplate;
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

        sendAuditLog(userId, "CREATE_TASK", "Task ID: " + saved.getId());

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

        sendAuditLog(userId, "UPDATE_TASK", "Task ID: " + id);

        return mapToResponse(updated);
    }

    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        taskRepository.delete(task);

        sendAuditLog(userId, "DELETE_TASK", "Task ID: " + id);
    }

    public void deleteAllUserTasks(Long userId) {
        taskRepository.deleteByUserId(userId);
        sendAuditLog(userId, "DELETE_ALL_TASKS", "All tasks deleted for user");
    }

    private void sendAuditLog(Long userId, String action, String details) {
        try {
            // Формируем JSON-запрос для user-service
            AuditLogRequest auditRequest = new AuditLogRequest(userId, action, details);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AuditLogRequest> entity = new HttpEntity<>(auditRequest, headers);
            restTemplate.postForEntity(userServiceUrl + "/api/audit/log", entity, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to send audit log: " + e.getMessage());
        }
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

    private static class AuditLogRequest {
        private Long userId;
        private String action;
        private String details;

        public AuditLogRequest(Long userId, String action, String details) {
            this.userId = userId;
            this.action = action;
            this.details = details;
        }

        public Long getUserId() { return userId; }
        public String getAction() { return action; }
        public String getDetails() { return details; }
    }
}