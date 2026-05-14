package org.example.taskservice.repository;

import org.example.taskservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByUserIdOrderByNameAsc(Long userId);
    Optional<Tag> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
    long deleteByIdAndUserId(Long id, Long userId);
}
