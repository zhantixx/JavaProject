package org.example.analyticsservice.repository;

import org.example.analyticsservice.entity.TaskStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<TaskStatsEntity, Long> {

    Optional<TaskStatsEntity> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskStatsEntity t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE TaskStatsEntity t SET " +
            "t.totalTasks = :totalTasks, " +
            "t.completedTasks = :completedTasks, " +
            "t.pendingTasks = :pendingTasks, " +
            "t.overdueTasks = :overdueTasks, " +
            "t.completionRate = :completionRate, " +
            "t.highPriorityTasks = :highPriorityTasks, " +
            "t.mediumPriorityTasks = :mediumPriorityTasks, " +
            "t.lowPriorityTasks = :lowPriorityTasks " +
            "WHERE t.userId = :userId")
    int updateStatsByUserId(@Param("userId") Long userId,
                            @Param("totalTasks") long totalTasks,
                            @Param("completedTasks") long completedTasks,
                            @Param("pendingTasks") long pendingTasks,
                            @Param("overdueTasks") long overdueTasks,
                            @Param("completionRate") double completionRate,
                            @Param("highPriorityTasks") long highPriorityTasks,
                            @Param("mediumPriorityTasks") long mediumPriorityTasks,
                            @Param("lowPriorityTasks") long lowPriorityTasks);
}