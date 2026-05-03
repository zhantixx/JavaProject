package org.example.analyticsservice.repository;

import org.example.analyticsservice.entity.TaskStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnalyticsRepository extends JpaRepository<TaskStatsEntity, Long> {
    Optional<TaskStatsEntity> findByUserId(Long userId);
}