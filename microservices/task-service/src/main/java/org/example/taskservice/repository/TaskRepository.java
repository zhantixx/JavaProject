package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    List<Task> findByUserIdAndDeadlineBetween(Long userId, LocalDateTime from, LocalDateTime to);

    List<Task> findByUserIdAndDeadlineGreaterThanEqual(Long userId, LocalDateTime from);

    List<Task> findByUserIdAndDeadlineLessThanEqual(Long userId, LocalDateTime to);

    List<Task> findByUserIdAndTags_Id(Long userId, Long tagId);

    List<Task> findByUserIdAndTags_IdAndDeadlineBetween(Long userId, Long tagId, LocalDateTime from, LocalDateTime to);

    List<Task> findByUserIdAndTags_IdAndDeadlineGreaterThanEqual(Long userId, Long tagId, LocalDateTime from);

    List<Task> findByUserIdAndTags_IdAndDeadlineLessThanEqual(Long userId, Long tagId, LocalDateTime to);
}