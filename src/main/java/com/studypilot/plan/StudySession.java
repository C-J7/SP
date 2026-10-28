package com.studypilot.plan;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_sessions")
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_id")
    private StudyPlan plan;

    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private Instant completedAt;

    protected StudySession() {
    }

    public StudySession(String topic, LocalDateTime startTime, LocalDateTime endTime) {
        this.topic = topic;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    void attachTo(StudyPlan plan) {
        this.plan = plan;
    }

    public void markCompleted(boolean done, Instant at) {
        this.completed = done;
        this.completedAt = done ? at : null;
    }

    public Duration duration() {
        return Duration.between(startTime, endTime);
    }

    public Long getId() { return id; }
    public StudyPlan getPlan() { return plan; }
    public String getTopic() { return topic; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
}
