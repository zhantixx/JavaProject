package org.example.taskservice.controller;

import org.example.taskservice.dto.TaskRequest;
import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getUserTasks() {
        return ResponseEntity.ok(taskService.getUserTasks());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}