package org.example.taskservice.repository;

import org.example.taskservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Список тегов пользователя, отсортированный по имени
    List<Tag> findAllByUserIdOrderByNameAsc(Long userId);

    // Поиск тега по id и владельцу
    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    // Проверка уникальности имени тега в рамках пользователя
    boolean existsByUserIdAndName(Long userId, String name);

    // Удаление тега, принадлежащего пользователю
    long deleteByIdAndUserId(Long id, Long userId);
}
