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
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.createTask(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                           @RequestParam(value = "from", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                           @RequestParam(value = "to", required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                           @RequestParam(value = "tagId", required = false) Long tagId) {
        if (userId == null) userId = 1L;

        if (tagId != null) {
            return ResponseEntity.ok(taskService.getUserTasksByTag(userId, tagId, from, to));
        }

        List<TaskResponse> result = (from == null && to == null)
                ? taskService.getUserTasks(userId)
                : taskService.getUserTasks(userId, from, to);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @Valid @RequestBody TaskRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(@PathVariable Long id,
                                                     @RequestBody TaskStatusUpdateRequest request,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.changeStatus(id, request.getStatus(), userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
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
                                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.addTagsToTask(id, request.tagIds(), userId));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<TaskResponse> removeTagFromTask(@PathVariable("id") Long id,
                                                          @PathVariable("tagId") Long tagId,
                                                          @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.removeTagFromTask(id, tagId, userId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(msg);
    }

    public static record AddTagsRequest(java.util.List<Long> tagIds) {}
}