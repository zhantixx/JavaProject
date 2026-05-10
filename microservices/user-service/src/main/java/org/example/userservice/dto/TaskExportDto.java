package org.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskExportDto {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String deadline;
}