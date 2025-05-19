package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

public class GenerateStudyPlanRequest {
    @NotNull
    private Long syllabusId;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Map<String, BusyHourDTO> busyHours;
    @NotNull
    private String userId;

    //getters and setters
    public Long getSyllabusId() { return syllabusId; }
    public void setSyllabusId(Long syllabusId) { this.syllabusId = syllabusId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Map<String, BusyHourDTO> getBusyHours() { return busyHours; }
    public void setBusyHours(Map<String, BusyHourDTO> busyHours) { this.busyHours = busyHours; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
