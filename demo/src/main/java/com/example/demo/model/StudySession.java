package com.example.demo.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDateTime;

@Embeddable
@Data
public class StudySession {
    private Long id;
    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private boolean reminderSent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}