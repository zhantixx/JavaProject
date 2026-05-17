package org.example.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleCalendarService {

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter RFC3339 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Value("${google.calendar.api.base-url:https://www.googleapis.com/calendar/v3}")
    private String baseUrl;

    public GoogleCalendarService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Создает событие в календаре primary (принимает LocalDateTime)
     *
     * @param accessToken - Google OAuth2 access token
     * @param summary - заголовок события
     * @param description - описание события
     * @param startDateTime - время начала (LocalDateTime)
     * @param endDateTime - время окончания (LocalDateTime)
     * @return id созданного события Google Calendar
     */
    public String createEvent(String accessToken,
                              String summary,
                              String description,
                              LocalDateTime startDateTime,
                              LocalDateTime endDateTime) {
        // Конвертируем LocalDateTime в RFC3339 строку с системным часовым поясом
        String startStr = startDateTime.atZone(ZoneId.systemDefault())
                .format(RFC3339);
        String endStr = endDateTime.atZone(ZoneId.systemDefault())
                .format(RFC3339);

        return createEvent(accessToken, summary, description, startStr, endStr, "UTC");
    }

    /**
     * Создает событие в календаре primary (принимает LocalDateTime с указанием часового пояса)
     *
     * @param accessToken - Google OAuth2 access token
     * @param summary - заголовок события
     * @param description - описание события
     * @param startDateTime - время начала (LocalDateTime)
     * @param endDateTime - время окончания (LocalDateTime)
     * @param timeZone - часовой пояс (например, "Asia/Almaty", "Europe/Moscow", "UTC")
     * @return id созданного события Google Calendar
     */
    public String createEvent(String accessToken,
                              String summary,
                              String description,
                              LocalDateTime startDateTime,
                              LocalDateTime endDateTime,
                              String timeZone) {
        // Конвертируем LocalDateTime в RFC3339 строку с указанным часовым поясом
        String startStr = startDateTime.atZone(ZoneId.of(timeZone))
                .format(RFC3339);
        String endStr = endDateTime.atZone(ZoneId.of(timeZone))
                .format(RFC3339);

        return createEvent(accessToken, summary, description, startStr, endStr, timeZone);
    }

    /**
     * Создает событие в календаре primary.
     * Время должно быть в формате RFC3339, например: 2026-05-11T10:00:00Z или 2026-05-11T10:00:00+03:00
     *
     * @param accessToken - Google OAuth2 access token
     * @param summary - заголовок события
     * @param description - описание события
     * @param startDateTime - время начала в формате RFC3339 (String)
     * @param endDateTime - время окончания в формате RFC3339 (String)
     * @return id созданного события Google Calendar
     */
    public String createEvent(String accessToken,
                              String summary,
                              String description,
                              String startDateTime,
                              String endDateTime) {
        return createEvent(accessToken, summary, description, startDateTime, endDateTime, "UTC");
    }

    /**
     * Создает событие в календаре primary.
     * Время должно быть в формате RFC3339, например: 2026-05-11T10:00:00Z или 2026-05-11T10:00:00+03:00
     *
     * @param accessToken - Google OAuth2 access token
     * @param summary - заголовок события
     * @param description - описание события
     * @param startDateTime - время начала в формате RFC3339 (String)
     * @param endDateTime - время окончания в формате RFC3339 (String)
     * @param timeZone - часовой пояс (например, "Asia/Almaty", "Europe/Moscow", "UTC")
     * @return id созданного события Google Calendar
     */
    public String createEvent(String accessToken,
                              String summary,
                              String description,
                              String startDateTime,
                              String endDateTime,
                              String timeZone) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        CreateEventRequest body = new CreateEventRequest();
        body.setSummary(summary);
        body.setDescription(description);
        body.setStart(new EventDateTime(startDateTime, timeZone));
        body.setEnd(new EventDateTime(endDateTime, timeZone));

        HttpEntity<CreateEventRequest> entity = new HttpEntity<>(body, headers);

        String url = baseUrl + "/calendars/primary/events";
        try {
            ResponseEntity<EventResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    EventResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
            throw new RuntimeException("Failed to create calendar event: " + response.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            String errorBody = ex.getResponseBodyAsString();
            throw new RuntimeException("Google Calendar API error: " + ex.getStatusCode() + " - " + errorBody, ex);
        }
    }

    // DTO для запроса
    public static class CreateEventRequest {
        private String summary;
        private String description;
        private EventDateTime start;
        private EventDateTime end;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public EventDateTime getStart() { return start; }
        public void setStart(EventDateTime start) { this.start = start; }
        public EventDateTime getEnd() { return end; }
        public void setEnd(EventDateTime end) { this.end = end; }
    }

    public static class EventDateTime {
        private String dateTime;
        private String timeZone;

        public EventDateTime() { }
        public EventDateTime(String dateTime, String timeZone) {
            this.dateTime = dateTime;
            this.timeZone = timeZone;
        }

        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
        public String getTimeZone() { return timeZone; }
        public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    }

    // DTO для ответа (нас интересует только id)
    public static class EventResponse {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}