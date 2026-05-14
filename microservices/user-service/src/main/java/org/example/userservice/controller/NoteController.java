package org.example.userservice.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.userservice.entity.Note;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    public NoteController(NoteService noteService, UserRepository userRepository) {
        this.noteService = noteService;
        this.userRepository = userRepository;
    }

    // Получить список заметок пользователя
    @GetMapping
    public List<NoteResponse> list(@PathVariable("userId") Long userId) {
        requireOwner(userId);
        return noteService.listUserNotes(userId).stream()
                .map(NoteResponse::from)
                .toList();
    }

    // Создать новую заметку
    @PostMapping
    public ResponseEntity<NoteResponse> create(@PathVariable("userId") Long userId,
                                               @Valid @RequestBody NoteRequest request) {
        requireOwner(userId);
        Note created = noteService.create(userId, request.title(), request.content());
        URI location = URI.create("/api/users/" + userId + "/notes/" + created.getId());
        return ResponseEntity.created(location).body(NoteResponse.from(created));
    }

    // Получить заметку по id (с проверкой принадлежности)
    @GetMapping("{id}")
    public NoteResponse get(@PathVariable("userId") Long userId,
                            @PathVariable("id") Long id) {
        requireOwner(userId);
        return NoteResponse.from(noteService.getOwned(userId, id));
    }

    // Обновить заметку
    @PutMapping("{id}")
    public NoteResponse update(@PathVariable("userId") Long userId,
                               @PathVariable("id") Long id,
                               @Valid @RequestBody UpdateNoteRequest request) {
        requireOwner(userId);
        Note updated = noteService.update(userId, id, request.title(), request.content());
        return NoteResponse.from(updated);
    }

    // Удалить заметку
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("id") Long id) {
        requireOwner(userId);
        noteService.delete(userId, id);
    }

    // Локальный обработчик "не найдено" -> 404 Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // DTO для входящих данных (создание)
    public static record NoteRequest(
            @Size(min = 1, max = 255, message = "Title length must be 1..255")
            String title,
            @NotNull(message = "Content is required")
            String content
    ) {}

    // DTO для входящих данных (обновление) — поля опциональны, но если заданы, валидируются
    public static record UpdateNoteRequest(
            @Size(min = 1, max = 255, message = "Title length must be 1..255")
            String title,
            String content
    ) {}

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

    // Проверка соответствия userId владельцу (из JWT берём email)
    private void requireOwner(Long pathUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = auth.getName();
        User current = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!current.getId().equals(pathUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
