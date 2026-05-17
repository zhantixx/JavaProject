package org.example.userservice.controller;

import org.example.userservice.dto.EventDto;
import org.example.userservice.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar/events")
public class CalendarEventController {

    private final CalendarEventService eventService;

    public CalendarEventController(CalendarEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto,
                                                @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.createEvent(eventDto, userId));
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.getEvents(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id,
                                                @RequestBody EventDto eventDto,
                                                @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(eventService.updateEvent(id, eventDto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        eventService.deleteEvent(id, userId);
        return ResponseEntity.ok().build();
    }
}
