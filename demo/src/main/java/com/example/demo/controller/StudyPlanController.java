package com.example.demo.controller;

import com.example.demo.dto.GenerateStudyPlanRequest;
import com.example.demo.dto.StudyPlanResponse;
import com.example.demo.model.StudyPlan;
import com.example.demo.service.StudyPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
@Tag(name = "Study Plan", description = "Endpoints for managing study plans")
public class StudyPlanController {

    @Autowired
    private StudyPlanService studyPlanService;


    @Operation(summary = "Generate a new study plan")
    @PostMapping("/create")
    public ResponseEntity<StudyPlanResponse> generateStudyPlan(
            @Valid @RequestBody GenerateStudyPlanRequest request) {

        StudyPlanResponse studyPlan = studyPlanService.generateStudyPlan(request);
        return ResponseEntity.status(201).body(studyPlan);
    }

    @Operation(summary = "Get a study plan by ID")
    @GetMapping("/{id}")
    public StudyPlan getStudyPlanById(
            @Parameter(description = "Study Plan ID") @PathVariable Long id) {
        return studyPlanService.getStudyPlanById(id);
    }
    
    @Operation(summary = "Get all study plans for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudyPlanResponse>> getUserStudyPlans(
            @Parameter(description = "User ID") @PathVariable String userId) {
        List<StudyPlanResponse> studyPlans = studyPlanService.getUserStudyPlans(userId);
        return ResponseEntity.ok(studyPlans);
    }

    @Operation(summary = "Get progress statistics for a user")
    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<Map<String, Object>> getUserProgressStats(
            @Parameter(description = "User ID") @PathVariable String userId) {
        Map<String, Object> stats = studyPlanService.getUserProgressStats(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Update session progress in a study plan")
    @PutMapping("/{studyPlanId}/session/{sessionId}")
    public ResponseEntity<StudyPlanResponse> updateSessionProgress(
            @Parameter(description = "Study Plan ID") @PathVariable Long studyPlanId,
            @Parameter(description = "Session ID") @PathVariable Long sessionId,
            @RequestParam boolean completed) {

        StudyPlanResponse updatedPlan = studyPlanService.updateSessionProgress(studyPlanId, sessionId, completed);
        return ResponseEntity.ok(updatedPlan);
    }


    @Operation(summary = "Delete a study plan by ID")
    @DeleteMapping("/{id}")
    public void deleteStudyPlan(
            @Parameter(description = "Study Plan ID") @PathVariable Long id) {
        studyPlanService.deleteStudyPlan(id);
    }
}