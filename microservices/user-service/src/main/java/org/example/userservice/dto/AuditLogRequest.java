package org.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {
    private Long userId;
    private String action;
    private String details;

    public Long getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setAction(String action) { this.action = action; }
    public void setDetails(String details) { this.details = details; }
}