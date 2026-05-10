//package org.example.userservice.service;
//
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class GoogleCalendarService {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private static final String CALENDAR_API_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
//
//    public String createEvent(String accessToken, String summary, String description,
//                              String startDateTime, String endDateTime) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String eventJson = String.format("""
//                {
//                    "summary": "%s",
//                    "description": "%s",
//                    "start": { "dateTime": "%s", "timeZone": "UTC" },
//                    "end": { "dateTime": "%s", "timeZone": "UTC" }
//                }
//                """, escapeJson(summary), escapeJson(description), startDateTime, endDateTime);
//
//        HttpEntity<String> entity = new HttpEntity<>(eventJson, headers);
//        ResponseEntity<String> response = restTemplate.exchange(
//                CALENDAR_API_URL, HttpMethod.POST, entity, String.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            String body = response.getBody();
//            int start = body.indexOf("\"id\":\"") + 6;
//            int end = body.indexOf("\"", start);
//            return body.substring(start, end);
//        } else {
//            throw new RuntimeException("Failed to create calendar event: " + response.getStatusCode());
//        }
//    }
//
//    private String escapeJson(String s) {
//        if (s == null) return "";
//        return s.replace("\\", "\\\\").replace("\"", "\\\"");
//    }
//}