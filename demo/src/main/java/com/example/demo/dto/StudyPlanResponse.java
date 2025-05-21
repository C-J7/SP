package com.example.demo.dto;

import com.example.demo.model.StudyPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Schema(description = "Study Plan Response DTO")
public class StudyPlanResponse {
    @Schema(description = "Study Plan ID")
    private Long id;
    @Schema(description = "Syllabus ID")
    private Long syllabusId;
    @Schema(description = "Start date")
    private LocalDate startDate;
    @Schema(description = "End date")
    private LocalDate endDate;
    @Schema(description = "User ID")
    private String userId;
    private List<StudySessionResponse> sessions;

    //Constructor to map StudyPlan to StudyPlanResponse
    public StudyPlanResponse(StudyPlan studyPlan) {
        this.id = studyPlan.getId();
        this.userId = studyPlan.getUserId();
        this.startDate = studyPlan.getStartDate();
        this.endDate = studyPlan.getEndDate();
        this.syllabusId = studyPlan.getSyllabus() != null ? studyPlan.getSyllabus().getId() : null;
        this.sessions = studyPlan.getSessions() != null
            ? studyPlan.getSessions().stream()
                .map(session -> new StudySessionResponse(session)) //lambda
                .collect(Collectors.toList())
            : null;
    }

    //getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getSyllabusId() {
        return syllabusId;
    }
    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public List<StudySessionResponse> getSessions() {
        return sessions;
    }
    public void setSessions(List<StudySessionResponse> sessions) {
        this.sessions = sessions;
    }
}
