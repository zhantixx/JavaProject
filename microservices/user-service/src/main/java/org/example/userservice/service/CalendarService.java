package org.example.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CalendarService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String calendarUrl = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    public void createEvent(String accessToken, String title, String description, String deadline) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = buildEventJson(title, description, deadline);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(calendarUrl, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google Calendar error: " + response.getStatusCode());
        }
    }

    private String buildEventJson(String title, String description, String deadline) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"summary\":\"").append(escapeJson(title)).append("\",");
        if (description != null) {
            json.append("\"description\":\"").append(escapeJson(description)).append("\",");
        }
        if (deadline != null) {
            json.append("\"start\":{ \"dateTime\":\"").append(deadline).append("\", \"timeZone\":\"").append(java.time.ZoneId.systemDefault().getId()).append("\"},");
            json.append("\"end\":{ \"dateTime\":\"").append(deadline).append("\", \"timeZone\":\"").append(java.time.ZoneId.systemDefault().getId()).append("\"}");
        } else {
            json.append("\"start\":{\"date\":\"2026-12-31\"},\"end\":{\"date\":\"2026-12-31\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"");
    }
}