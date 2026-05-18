package org.example.aiservice.dto;

public class RecommendationItemResponse {

    private String title;
    private String detail;
    private String action;

    public RecommendationItemResponse() {
    }

    public RecommendationItemResponse(String title, String detail, String action) {
        this.title = title;
        this.detail = detail;
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
