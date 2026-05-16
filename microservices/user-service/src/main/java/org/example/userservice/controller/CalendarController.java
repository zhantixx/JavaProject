package org.example.userservice.controller;

import org.example.userservice.dto.EventDto;
import org.example.userservice.service.GoogleCalendarService;
import org.example.userservice.service.OAuthAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final GoogleCalendarService calendarService;
    private final OAuthAccountService oAuthAccountService;

    public CalendarController(GoogleCalendarService calendarService,
                              OAuthAccountService oAuthAccountService) {
        this.calendarService = calendarService;
        this.oAuthAccountService = oAuthAccountService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody EventDto dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername();

        try {
            // Получаем актуальный Google access token
            String googleAccessToken = oAuthAccountService.getValidGoogleAccessToken(email);

            // Создаем событие в Google Calendar
            String eventId = calendarService.createEvent(
                    googleAccessToken,
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getStartDateTime(),
                    dto.getEndDateTime()
            );

            return ResponseEntity.ok(Map.of("eventId", eventId, "message", "Event created"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}