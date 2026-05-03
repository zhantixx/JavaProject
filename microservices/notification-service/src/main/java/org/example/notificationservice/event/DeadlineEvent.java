package org.example.notificationservice.event;

public class DeadlineEvent {
    private Long taskId;
    private String taskTitle;
    private String deadline;
    private String userEmail;
    private String userName;
    private String eventType;

    public DeadlineEvent() {}

    public DeadlineEvent(Long taskId, String taskTitle, String deadline, String userEmail, String userName, String eventType) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.deadline = deadline;
        this.userEmail = userEmail;
        this.userName = userName;
        this.eventType = eventType;
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}