package org.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWithTasksExportDto {
    private UserExportDto user;
    private List<TaskExportDto> tasks;
}