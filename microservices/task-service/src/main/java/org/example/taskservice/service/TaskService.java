package org.example.taskservice.service;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.entity.Tag;
import org.example.taskservice.mapper.TaskMapper;
import org.example.taskservice.repository.TagRepository;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final RestTemplate restTemplate;
    private final TaskMapper taskMapper;

    @Value("${user.service.url:http://user-service:8081}")
    private String userServiceUrl;

    public TaskService(TaskRepository taskRepository,
                       TagRepository tagRepository,
                       RestTemplate restTemplate,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.restTemplate = restTemplate;
        this.taskMapper = taskMapper;
    }

    public TaskResponse createTask(TaskRequest request, Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid User ID: " + userId);
        }

        System.out.println("Creating task for User ID: " + userId);
        System.out.println("Task request details: " + request);

        try {
            Task task = new Task();
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setPriority(request.getPriority());
            task.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNED");
            task.setDeadline(request.getDeadline());
            task.setUserId(userId);

            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                Set<Tag> tags = resolveTags(userId, request.getTagIds());
                task.setTags(tags);
            }

            Task saved = taskRepository.save(task);
            System.out.println("Task successfully saved for User ID: " + userId);
            sendAuditLog(userId, "CREATE_TASK", "Task ID: " + saved.getId());
            return taskMapper.toResponse(saved);
        } catch (Exception e) {
            System.err.println("Error while creating task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create task", e);
        }
    }

    public List<TaskResponse> getUserTasks(Long userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getUserTasks(Long userId, LocalDateTime from, LocalDateTime to) {
        List<Task> tasks;
        if (from != null && to != null) {
            tasks = taskRepository.findByUserIdAndDeadlineBetween(userId, from, to);
        } else if (from != null) {
            tasks = taskRepository.findByUserIdAndDeadlineGreaterThanEqual(userId, from);
        } else if (to != null) {
            tasks = taskRepository.findByUserIdAndDeadlineLessThanEqual(userId, to);
        } else {
            tasks = taskRepository.findByUserId(userId);
        }
        return tasks.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getUserTasksByTag(Long userId, Long tagId, LocalDateTime from, LocalDateTime to) {
        List<Task> tasks;
        if (from != null && to != null) {
            tasks = taskRepository.findByUserIdAndTags_IdAndDeadlineBetween(userId, tagId, from, to);
        } else if (from != null) {
            tasks = taskRepository.findByUserIdAndTags_IdAndDeadlineGreaterThanEqual(userId, tagId, from);
        } else if (to != null) {
            tasks = taskRepository.findByUserIdAndTags_IdAndDeadlineLessThanEqual(userId, tagId, to);
        } else {
            tasks = taskRepository.findByUserIdAndTags_Id(userId, tagId);
        }
        return tasks.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long id, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDeadline(request.getDeadline());

        if (request.getTagIds() != null) {
            Set<Tag> tags = resolveTags(userId, request.getTagIds());
            task.setTags(tags);
        }

        Task updated = taskRepository.save(task);
        sendAuditLog(userId, "UPDATE_TASK", "Task ID: " + id);
        return taskMapper.toResponse(updated);
    }

    public TaskResponse changeStatus(Long id, String newStatus, Long userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }

        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);
        sendAuditLog(userId, "CHANGE_STATUS", "Task ID: " + id + " -> " + newStatus);
        return taskMapper.toResponse(updated);
    }

    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }
        taskRepository.delete(task);
        sendAuditLog(userId, "DELETE_TASK", "Task ID: " + id);
    }

    public TaskResponse addTagsToTask(Long taskId, List<Long> tagIds, Long userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }
        Set<Tag> tags = resolveTags(userId, tagIds);
        task.getTags().addAll(tags);
        Task saved = taskRepository.save(task);
        sendAuditLog(userId, "ADD_TAGS", "Task ID: " + taskId);
        return taskMapper.toResponse(saved);
    }

    public TaskResponse removeTagFromTask(Long taskId, Long tagId, Long userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this task");
        }
        task.getTags().removeIf(t -> t.getId().equals(tagId));
        Task saved = taskRepository.save(task);
        sendAuditLog(userId, "REMOVE_TAG", "Task ID: " + taskId);
        return taskMapper.toResponse(saved);
    }

    public void deleteAllUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        taskRepository.deleteAll(tasks);
        sendAuditLog(userId, "DELETE_ALL_TASKS", "All tasks deleted");
    }

    private Set<Tag> resolveTags(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(tagRepository.findAllById(tagIds));
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
