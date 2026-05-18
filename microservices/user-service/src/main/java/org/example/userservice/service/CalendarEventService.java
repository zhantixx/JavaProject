package org.example.userservice.service;

import org.example.userservice.dto.EventDto;
import org.example.userservice.entity.Event;
import org.example.userservice.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarEventService {

    private final EventRepository eventRepository;

    public CalendarEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventDto createEvent(EventDto dto, Long userId) {
        validateEventTime(dto);
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setUserId(userId);

        event = eventRepository.save(event);
        return mapToDto(event);
    }

    public List<EventDto> getEvents(Long userId) {
        return eventRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EventDto updateEvent(Long id, EventDto dto, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found."));
        if (!event.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        validateEventTime(dto);

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());

        event = eventRepository.save(event);
        return mapToDto(event);
    }

    public void deleteEvent(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found."));
        if (!event.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        eventRepository.delete(event);
    }

    private EventDto mapToDto(Event event) {
        EventDto dto = new EventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartDateTime(event.getStartDateTime());
        dto.setEndDateTime(event.getEndDateTime());
        return dto;
    }

    private void validateEventTime(EventDto dto) {
        if (dto.getStartDateTime() == null || dto.getEndDateTime() == null) {
            throw new IllegalArgumentException("Start and end time are required");
        }
        if (dto.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Past dates are not allowed");
        }
        if (!dto.getStartDateTime().isBefore(dto.getEndDateTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
}
