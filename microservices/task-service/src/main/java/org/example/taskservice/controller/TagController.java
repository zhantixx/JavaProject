package org.example.taskservice.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.taskservice.entity.Tag;
import org.example.taskservice.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        List<TagResponse> result = tagService.list(userId).stream()
                .map(TagResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                              @RequestBody TagRequest request) {
        if (userId == null) userId = 1L;
        Tag created = tagService.create(userId, request.name(), request.color());
        return ResponseEntity.status(HttpStatus.CREATED).body(TagResponse.from(created));
    }

    @GetMapping("{id}")
    public ResponseEntity<TagResponse> get(@PathVariable("id") Long id,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        Tag tag = tagService.getOwned(userId, id);
        return ResponseEntity.ok(TagResponse.from(tag));
    }

    @PutMapping("{id}")
    public ResponseEntity<TagResponse> update(@PathVariable("id") Long id,
                                              @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                              @RequestBody TagUpdateRequest request) {
        if (userId == null) userId = 1L;
        Tag updated = tagService.update(userId, id, request.name(), request.color());
        return ResponseEntity.ok(TagResponse.from(updated));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) userId = 1L;
        tagService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    public static record TagRequest(String name, String color) {}
    public static record TagUpdateRequest(String name, String color) {}

    public static record TagResponse(
            Long id,
            Long userId,
            String name,
            String color,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static TagResponse from(Tag t) {
            return new TagResponse(
                    t.getId(),
                    t.getUserId(),
                    t.getName(),
                    t.getColor(),
                    t.getCreatedAt(),
                    t.getUpdatedAt()
            );
        }
    }
}
