package org.example.taskservice.service;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
        task.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNED");
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

    // Новый метод фильтрации по срокам: deadline ∈ [from, to] (границы включительно), параметры опциональны
    public List<TaskResponse> getUserTasks(Long userId, LocalDateTime from, LocalDateTime to) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream()
                .filter(t -> {
                    if (t.getDeadline() == null) return false;
                    boolean ok = true;
                    if (from != null) {
                        ok = ok && !t.getDeadline().isBefore(from); // deadline >= from
                    }
                    if (to != null) {
                        ok = ok && !t.getDeadline().isAfter(to);    // deadline <= to
                    }
                    return ok;
                })
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

    public TaskResponse changeStatus(Long id, String newStatus, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        String current = task.getStatus() == null ? "PLANNED" : task.getStatus().toUpperCase();
        String target = newStatus == null ? current : newStatus.toUpperCase();

        // Допустимые переходы
        Set<String> allowedTargets =
                switch (current) {
                    case "PLANNED" -> Set.of("IN_PROGRESS", "CANCELED");
                    case "IN_PROGRESS" -> Set.of("DONE", "BLOCKED", "CANCELED");
                    case "BLOCKED" -> Set.of("IN_PROGRESS", "CANCELED");
                    case "OVERDUE" -> Set.of("IN_PROGRESS", "DONE", "CANCELED");
                    default -> Set.of(); // DONE/CANCELED — финальные
                };

        if (current.equals("DONE") || current.equals("CANCELED")) {
            throw new IllegalArgumentException("Task is in a final state: " + current);
        }
        if (!allowedTargets.contains(target)) {
            throw new IllegalArgumentException("Illegal status transition: " + current + " -> " + target);
        }

        task.setStatus(target);
        Task updated = taskRepository.save(task);
        sendAuditLog(userId, "CHANGE_STATUS", "Task ID: " + id + " -> " + target);
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
        String status = task.getStatus();
        LocalDateTime dl = task.getDeadline();
        if (status == null) {
            status = "PLANNED";
        }
        String normalized = status.toUpperCase();
        if (!normalized.equals("DONE") && !normalized.equals("CANCELED")
                && dl != null && dl.isBefore(LocalDateTime.now())) {
            normalized = "OVERDUE";
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                normalized,
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