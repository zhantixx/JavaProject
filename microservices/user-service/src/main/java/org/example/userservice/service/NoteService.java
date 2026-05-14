package org.example.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.userservice.entity.Note;
import org.example.userservice.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // Список заметок пользователя (свежие сверху)
    @Transactional(readOnly = true)
    public List<Note> listUserNotes(Long userId) {
        requireUserId(userId);
        return noteRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
    }

    // Создание заметки
    public Note create(Long userId, String title, String content) {
        requireUserId(userId);
        String safeTitle = normalizeTitle(title);
        String safeContent = normalizeContent(content);
        Note note = new Note(userId, safeTitle, safeContent);
        return noteRepository.save(note);
    }

    // Получение заметки по id с проверкой принадлежности
    @Transactional(readOnly = true)
    public Note getOwned(Long userId, Long noteId) {
        requireUserId(userId);
        requireNoteId(noteId);
        return noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
    }

    // Обновление заметки (title/content), если принадлежит пользователю
    public Note update(Long userId, Long noteId, String title, String content) {
        requireUserId(userId);
        requireNoteId(noteId);
        Note existing = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
        if (title != null) {
            existing.setTitle(normalizeTitle(title));
        }
        if (content != null) {
            existing.setContent(normalizeContent(content));
        }
        return noteRepository.save(existing);
    }

    // Удаление заметки, если принадлежит пользователю
    public void delete(Long userId, Long noteId) {
        requireUserId(userId);
        requireNoteId(noteId);
        long deleted = noteRepository.deleteByIdAndUserId(noteId, userId);
        if (deleted == 0) {
            throw new EntityNotFoundException("Note not found");
        }
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    private void requireNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("noteId is required");
        }
    }

    private String normalizeTitle(String title) {
        String t = Objects.toString(title, "").trim();
        // Ограничим длину заголовка до 255 символов (соответствует ограничениям в сущности)
        return t.length() <= 255 ? t : t.substring(0, 255);
    }

    private String normalizeContent(String content) {
        return Objects.toString(content, "").trim();
    }
}
