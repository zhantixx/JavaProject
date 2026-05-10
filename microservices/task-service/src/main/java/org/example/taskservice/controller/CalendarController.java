package org.example.taskservice.controller;

import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.service.CalendarIcsService;
import org.example.taskservice.service.TaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final TaskService taskService;
    private final CalendarIcsService icsService;

    public CalendarController(TaskService taskService, CalendarIcsService icsService) {
        this.taskService = taskService;
        this.icsService = icsService;
    }

    // Ссылка подписки для календарей: добавьте URL этого эндпоинта в Google/Outlook/iOS как подписку по ссылке
    @GetMapping(value = "/ics", produces = "text/calendar")
    public ResponseEntity<byte[]> myTasksIcs(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        List<TaskResponse> tasks = taskService.getUserTasks(userId);
        String ics = icsService.buildIcs(userId, tasks);
        byte[] bytes = ics.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tasks.ics\"")
                .contentType(new MediaType("text", "calendar"))
                .body(bytes);
    }
}
