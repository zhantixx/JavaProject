package org.example.analyticsservice.dto;

public class TaskStatsDto {
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
    private double completionRate;
    private long highPriorityTasks;
    private long mediumPriorityTasks;
    private long lowPriorityTasks;

    public TaskStatsDto() {}

    public TaskStatsDto(long totalTasks, long completedTasks, long pendingTasks, long overdueTasks, double completionRate, long highPriorityTasks, long mediumPriorityTasks, long lowPriorityTasks) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.overdueTasks = overdueTasks;
        this.completionRate = completionRate;
        this.highPriorityTasks = highPriorityTasks;
        this.mediumPriorityTasks = mediumPriorityTasks;
        this.lowPriorityTasks = lowPriorityTasks;
    }

    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    public long getHighPriorityTasks() { return highPriorityTasks; }
    public void setHighPriorityTasks(long highPriorityTasks) { this.highPriorityTasks = highPriorityTasks; }
    public long getMediumPriorityTasks() { return mediumPriorityTasks; }
    public void setMediumPriorityTasks(long mediumPriorityTasks) { this.mediumPriorityTasks = mediumPriorityTasks; }
    public long getLowPriorityTasks() { return lowPriorityTasks; }
    public void setLowPriorityTasks(long lowPriorityTasks) { this.lowPriorityTasks = lowPriorityTasks; }
}