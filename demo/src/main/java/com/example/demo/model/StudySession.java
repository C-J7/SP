package com.example.demo.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDateTime;

@Embeddable
@Data
public class StudySession {
    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;
    private boolean reminderSent;
} 