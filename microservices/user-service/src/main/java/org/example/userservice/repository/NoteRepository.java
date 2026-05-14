package org.example.userservice.repository;

import org.example.userservice.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Список заметок пользователя, отсортированный по времени обновления (свежие сверху)
    List<Note> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    // Поиск заметки по идентификатору и владельцу (для контроля доступа)
    Optional<Note> findByIdAndUserId(Long id, Long userId);

    // Удаление заметки, гарантируя принадлежность пользователю
    long deleteByIdAndUserId(Long id, Long userId);

    // Проверка существования заметки с привязкой к владельцу
    boolean existsByIdAndUserId(Long id, Long userId);
}
