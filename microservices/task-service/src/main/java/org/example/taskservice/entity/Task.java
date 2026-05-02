package org.example.taskservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime deadline;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}