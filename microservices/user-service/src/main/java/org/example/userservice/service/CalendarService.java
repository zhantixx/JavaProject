package org.example.userservice.service;

import org.example.userservice.dto.EventDto;
import org.example.userservice.entity.Event;
import org.example.userservice.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CalendarService {

    private final UserEventRepository eventRepository;

    public CalendarService(UserEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event createEvent(Long userId, EventDto dto) {
        // Валидация времени
        if (dto.getStartDateTime().isAfter(dto.getEndDateTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Event event = new Event();
        event.setUserId(userId);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> getUserEvents(Long userId, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return eventRepository.findByUserIdAndStartDateTimeBetween(userId, start, end);
        }
        return eventRepository.findByUserIdOrderByStartDateTimeAsc(userId);
    }

    @Transactional
    public Event updateEvent(Long eventId, Long userId, EventDto dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this event");
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        // Убрал вызовы setColor() и setLocation(), так как они отсутствуют в модели.

        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getUserId().equals(userId)) {
            throw new RuntimeException("You don't own this event");
        }

        eventRepository.delete(event);
    }

    @Transactional
    public void deleteAllUserEvents(Long userId) {
        eventRepository.deleteByUserId(userId);
    }
}
