package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "study_plans")
public class StudyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;

    private LocalDate startDate;
    private LocalDate endDate;
    private String userId;

    @ElementCollection
    @CollectionTable(name = "study_sessions", joinColumns = @JoinColumn(name = "study_plan_id"))
    private List<StudySession> sessions;
} 