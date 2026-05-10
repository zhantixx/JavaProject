package org.example.taskservice.controller;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // --- CRUD операции ---
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.createTask(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.getUserTasks(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @RequestBody TaskRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return ResponseEntity.ok(taskService.updateTask(id, request, userId));
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
}