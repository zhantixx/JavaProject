package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    // Фильтрация по срокам: deadline в диапазоне [from, to]
    List<Task> findByUserIdAndDeadlineBetween(Long userId, LocalDateTime from, LocalDateTime to);

    // deadline >= from
    List<Task> findByUserIdAndDeadlineGreaterThanEqual(Long userId, LocalDateTime from);

    // deadline <= to
    List<Task> findByUserIdAndDeadlineLessThanEqual(Long userId, LocalDateTime to);

    // Фильтрация по тегу: задачи пользователя, содержащие указанный тег
    List<Task> findByUserIdAndTags_Id(Long userId, Long tagId);

    // По тегу и срокам: deadline в диапазоне [from, to]
    List<Task> findByUserIdAndTags_IdAndDeadlineBetween(Long userId, Long tagId, LocalDateTime from, LocalDateTime to);

    // По тегу: deadline >= from
    List<Task> findByUserIdAndTags_IdAndDeadlineGreaterThanEqual(Long userId, Long tagId, LocalDateTime from);

    // По тегу: deadline <= to
    List<Task> findByUserIdAndTags_IdAndDeadlineLessThanEqual(Long userId, Long tagId, LocalDateTime to);
}