package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.model.StudySession;

public class StudySessionResponse {
    private Long id;
    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private boolean reminderSent;
    
    //Constructor to initialize StudySessionResponse from StudySession
    public StudySessionResponse(StudySession session) {
        this.id = session.getId();
        this.topic = session.getTopic();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.completed = session.isCompleted();
        this.reminderSent = session.isReminderSent();
    }
    
    //getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public boolean isCompleted() {
        return completed;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public boolean isReminderSent() {
        return reminderSent;
    }
    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }
}
