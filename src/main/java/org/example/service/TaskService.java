package org.example.service;

import org.example.dto.TaskRequest;
import org.example.dto.TaskResponse;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.event.DeadlineEvent;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    @CacheEvict(value = "userTasks", allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        task.setDeadline(request.getDeadline());
        task.setUser(currentUser);

        Task saved = taskRepository.save(task);

        sendNotification(saved, currentUser, "CREATED");

        return mapToResponse(saved);
    }

    @Cacheable(value = "userTasks", key = "#root.methodName + '_' + authentication.name", unless = "#result.isEmpty()")
    public List<TaskResponse> getUserTasks() {
        System.out.println("Загрузка задач из БД (не из кэша)");
        User currentUser = getCurrentUser();
        return taskRepository.findByUser(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "userTasks", allEntries = true)
    public TaskResponse updateTask(Long id, TaskRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't own this task");
        }

        LocalDateTime oldDeadline = task.getDeadline();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDeadline(request.getDeadline());

        Task updated = taskRepository.save(task);

        sendNotification(updated, currentUser, "UPDATED");

        if (oldDeadline != null && !oldDeadline.equals(updated.getDeadline())) {
            sendNotification(updated, currentUser, "DEADLINE_CHANGED");
        }

        return mapToResponse(updated);
    }

    @CacheEvict(value = "userTasks", allEntries = true)
    public void deleteTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't own this task");
        }

        sendNotification(task, currentUser, "DELETED");

        taskRepository.delete(task);
    }

    @CacheEvict(value = "userTasks", allEntries = true)
    public void clearCache() {
        System.out.println("Кэш задач очищен");
    }

    private void sendNotification(Task task, User user, String eventType) {
        try {
            DeadlineEvent event = new DeadlineEvent(
                    task.getId(),
                    task.getTitle(),
                    task.getDeadline(),
                    user.getEmail(),
                    user.getName(),
                    eventType
            );
            notificationProducer.sendDeadlineNotification(event);
        } catch (Exception e) {
            System.err.println("Ошибка отправки уведомления в Kafka: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDeadline(task.getDeadline());
        response.setUserId(task.getUser().getId());
        return response;
    }
}