package org.example.userservice.dto;

import java.util.List;

public class UserWithTasksExportDto {
    private UserExportDto user;
    private List<TaskExportDto> tasks;

    public UserWithTasksExportDto() {}

    public UserWithTasksExportDto(UserExportDto user, List<TaskExportDto> tasks) {
        this.user = user;
        this.tasks = tasks;
    }

    public UserExportDto getUser() { return user; }
    public List<TaskExportDto> getTasks() { return tasks; }

    public void setUser(UserExportDto user) { this.user = user; }
    public void setTasks(List<TaskExportDto> tasks) { this.tasks = tasks; }
}