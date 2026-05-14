package org.example.taskservice.service;

import org.example.taskservice.dto.TaskResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CalendarIcsService {

    private static final DateTimeFormatter UTC_TS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String buildIcs(Long userId, List<TaskResponse> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//personal-assistant//tasks//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("X-WR-CALNAME:Tasks User ").append(userId == null ? "" : userId).append("\r\n");

        String nowUtc = toUtc(LocalDateTime.now());

        for (TaskResponse t : tasks) {
            if (t.getDeadline() == null) {
                // Можно сделать all-day по сегодняшней дате, но лучше пропустить без дедлайна
                continue;
            }
            String status = t.getStatus() == null ? "PLANNED" : t.getStatus().toUpperCase();
            String prefix = switch (status) {
                case "DONE" -> "[DONE] ";
                case "OVERDUE" -> "[OVERDUE] ";
                case "IN_PROGRESS" -> "[DOING] ";
                case "BLOCKED" -> "[BLOCKED] ";
                default -> "";
            };

            // DTSTART/DTEND: событие длительностью 1 час, заканчивающееся в дедлайн
            LocalDateTime end = t.getDeadline();
            LocalDateTime start = end.minusHours(1);

            String dtStart = toUtc(start);
            String dtEnd = toUtc(end);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:task-").append(t.getId()).append("@personal-assistant\r\n");
            sb.append("DTSTAMP:").append(nowUtc).append("\r\n");
            sb.append("DTSTART:").append(dtStart).append("\r\n");
            sb.append("DTEND:").append(dtEnd).append("\r\n");
            sb.append("SUMMARY:").append(escape(prefix + nullSafe(t.getTitle()))).append("\r\n");
            if (t.getDescription() != null && !t.getDescription().isBlank()) {
                sb.append("DESCRIPTION:").append(escape(t.getDescription())).append("\r\n");
            }
            sb.append("STATUS:CONFIRMED\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String toUtc(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).format(UTC_TS);
    }

    private String escape(String s) {
        String v = s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\n")
                .replace(",", "\\,")
                .replace(";", "\\;");
        // Разбивка длинных строк (soft line breaks)
        return fold(v, 74);
    }

    private String fold(String s, int max) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int end = Math.min(i + max, s.length());
            out.append(s, i, end);
            if (end < s.length()) {
                out.append("\r\n ");
            }
            i = end;
        }
        return out.toString();
    }

    private String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
