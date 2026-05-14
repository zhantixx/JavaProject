package org.example.userservice.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.userservice.entity.Note;
import org.example.userservice.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Получить список заметок пользователя
    @GetMapping
    public List<NoteResponse> list(@PathVariable("userId") Long userId) {
        return noteService.listUserNotes(userId).stream()
                .map(NoteResponse::from)
                .toList();
    }

    // Создать новую заметку
    @PostMapping
    public ResponseEntity<NoteResponse> create(@PathVariable("userId") Long userId,
                                               @RequestBody NoteRequest request) {
        Note created = noteService.create(userId, request.title(), request.content());
        URI location = URI.create("/api/users/" + userId + "/notes/" + created.getId());
        return ResponseEntity.created(location).body(NoteResponse.from(created));
    }

    // Получить заметку по id (с проверкой принадлежности)
    @GetMapping("{id}")
    public NoteResponse get(@PathVariable("userId") Long userId,
                            @PathVariable("id") Long id) {
        return NoteResponse.from(noteService.getOwned(userId, id));
    }

    // Обновить заметку
    @PutMapping("{id}")
    public NoteResponse update(@PathVariable("userId") Long userId,
                               @PathVariable("id") Long id,
                               @RequestBody NoteRequest request) {
        Note updated = noteService.update(userId, id, request.title(), request.content());
        return NoteResponse.from(updated);
    }

    // Удалить заметку
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("id") Long id) {
        noteService.delete(userId, id);
    }

    // Локальный обработчик "не найдено" -> 404 Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // DTO для входящих данных
    public static record NoteRequest(String title, String content) {}

    // DTO для исходящих данных
    public static record NoteResponse(
            Long id,
            Long userId,
            String title,
            String content,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static NoteResponse from(Note n) {
            return new NoteResponse(
                    n.getId(),
                    n.getUserId(),
                    n.getTitle(),
                    n.getContent(),
                    n.getCreatedAt(),
                    n.getUpdatedAt()
            );
        }
    }
}
