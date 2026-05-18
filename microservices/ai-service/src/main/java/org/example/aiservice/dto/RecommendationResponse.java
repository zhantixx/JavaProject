package org.example.aiservice.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponse {

    private String mode;
    private String summary;
    private List<RecommendationItemResponse> recommendations = new ArrayList<>();

    public RecommendationResponse() {
    }

    public RecommendationResponse(String mode, String summary, List<RecommendationItemResponse> recommendations) {
        this.mode = mode;
        this.summary = summary;
        this.recommendations = recommendations;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<RecommendationItemResponse> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationItemResponse> recommendations) {
        this.recommendations = recommendations;
    }
}
