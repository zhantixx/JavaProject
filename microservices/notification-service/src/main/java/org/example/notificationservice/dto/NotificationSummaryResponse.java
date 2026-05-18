package org.example.notificationservice.dto;

public class NotificationSummaryResponse {
    private long total;
    private long unread;

    public NotificationSummaryResponse() {
    }

    public NotificationSummaryResponse(long total, long unread) {
        this.total = total;
        this.unread = unread;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getUnread() { return unread; }
    public void setUnread(long unread) { this.unread = unread; }
}
