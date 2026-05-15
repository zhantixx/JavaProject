package org.example.userservice.controller;

import org.example.userservice.dto.EventDto;
import org.example.userservice.service.GoogleCalendarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final GoogleCalendarService calendarService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public CalendarController(GoogleCalendarService calendarService,
                              OAuth2AuthorizedClientService authorizedClientService) {
        this.calendarService = calendarService;
        this.authorizedClientService = authorizedClientService;
    }

    @PostMapping("/events")
    public String createEvent(@RequestBody EventDto dto, @AuthenticationPrincipal OAuth2User oAuth2User) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "google", oAuth2User.getName());
        String accessToken = client.getAccessToken().getTokenValue();
        return calendarService.createEvent(accessToken, dto.getTitle(), dto.getDescription(),
                dto.getStartDateTime(), dto.getEndDateTime());
    }

    @PostMapping("/create-from-task")
    public String createEventFromTask(@AuthenticationPrincipal OAuth2User oAuth2User,
                                      @RequestParam String title,
                                      @RequestParam(required = false) String description,
                                      @RequestParam String startDateTime,
                                      @RequestParam String endDateTime) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "google", oAuth2User.getName());
        String accessToken = client.getAccessToken().getTokenValue();
        return calendarService.createEvent(accessToken, title, description, startDateTime, endDateTime);
    }
}