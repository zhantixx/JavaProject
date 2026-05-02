package org.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineEvent {
    private Long taskId;
    private String taskTitle;
    private LocalDateTime deadline;
    private String userEmail;
    private String userName;
    private String eventType;
}
