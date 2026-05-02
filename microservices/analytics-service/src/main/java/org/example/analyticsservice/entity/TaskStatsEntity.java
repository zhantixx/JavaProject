package org.example.analyticsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_tasks")
    private long totalTasks;

    @Column(name = "completed_tasks")
    private long completedTasks;

    @Column(name = "pending_tasks")
    private long pendingTasks;

    @Column(name = "overdue_tasks")
    private long overdueTasks;

    @Column(name = "completion_rate")
    private double completionRate;

    @Column(name = "high_priority_tasks")
    private long highPriorityTasks;

    @Column(name = "medium_priority_tasks")
    private long mediumPriorityTasks;

    @Column(name = "low_priority_tasks")
    private long lowPriorityTasks;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}