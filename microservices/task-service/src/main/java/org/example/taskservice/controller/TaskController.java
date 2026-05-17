package org.example.taskservice.controller;

import jakarta.validation.Valid;
import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.dto.TaskStatusUpdateRequest;
import org.example.taskservice.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        System.out.println("Received POST /api/tasks with User ID Header: " + userIdHeader);
        System.out.println("Task request body: " + request);

        try {
            Long userId = parseUserId(userIdHeader);
            TaskResponse response = taskService.createTask(request, userId);
            System.out.println("Task successfully created for User ID: " + userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error during task creation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    private Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X-User-Id header: " + userIdHeader);
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                           @RequestParam(value = "from", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                           @RequestParam(value = "to", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                           @RequestParam(value = "tagId", required = false) Long tagId) {
        System.out.println("Received GET /api/tasks with User ID Header: " + userIdHeader);

        Long userId = parseUserId(userIdHeader);

        if (tagId != null) {
            System.out.println("Filtering tasks by Tag ID: " + tagId);
            return ResponseEntity.ok(taskService.getUserTasksByTag(userId, tagId, from, to));
        }

        System.out.println("Retrieving tasks for User ID: " + userId);
        List<TaskResponse> result = (from == null && to == null)
                ? taskService.getUserTasks(userId)
                : taskService.getUserTasks(userId, from, to);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(@PathVariable Long id,
                                                     @RequestBody TaskStatusUpdateRequest request,
                                                     @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return ResponseEntity.ok(taskService.changeStatus(id, request.getStatus(), userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getUserTasks(userId));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllTasksOfUser(@PathVariable Long userId) {
        taskService.deleteAllUserTasks(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<TaskResponse> addTagsToTask(@PathVariable("id") Long id,
                                                      @RequestBody AddTagsRequest request,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return ResponseEntity.ok(taskService.addTagsToTask(id, request.tagIds(), userId));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<TaskResponse> removeTagFromTask(@PathVariable("id") Long id,
                                                          @PathVariable("tagId") Long tagId,
                                                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return ResponseEntity.ok(taskService.removeTagFromTask(id, tagId, userId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        System.err.println("Task Controller error: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.internalServerError().body("Error: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        System.err.println("Illegal argument: " + ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid argument: " + ex.getMessage());
    }

    public static record AddTagsRequest(java.util.List<Long> tagIds) {}
}
