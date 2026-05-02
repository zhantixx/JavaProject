package org.example.taskservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime deadline;
}
