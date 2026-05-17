package org.example.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TaskStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PLANNED|DONE|CANCELED", message = "Status must be PLANNED, DONE or CANCELED")
    private String status;

    public TaskStatusUpdateRequest() {}

    public TaskStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
