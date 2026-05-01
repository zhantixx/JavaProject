package org.example.repository;


import org.example.entity.Task;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
    List<Task> findByDeadlineBeforeAndStatusNot(LocalDateTime date, String status);
}