package org.example.aiservice.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationRequest {

    private String userName;
    private List<RecommendationTaskInput> tasks = new ArrayList<>();
    private List<RecommendationNoteInput> notes = new ArrayList<>();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<RecommendationTaskInput> getTasks() {
        return tasks;
    }

    public void setTasks(List<RecommendationTaskInput> tasks) {
        this.tasks = tasks;
    }

    public List<RecommendationNoteInput> getNotes() {
        return notes;
    }

    public void setNotes(List<RecommendationNoteInput> notes) {
        this.notes = notes;
    }
}
