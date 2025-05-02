package com.example.demo.controller;

import com.example.demo.model.StudyPlan;
import com.example.demo.service.StudyPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
@CrossOrigin(origins = "*")
public class StudyPlanController {
    
    @Autowired
    private StudyPlanService studyPlanService;
    
    @PostMapping("/generate")
    public ResponseEntity<StudyPlan> generateStudyPlan(
            @RequestParam Long syllabusId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestBody Map<String, LocalDateTime> busyHours,
            @RequestParam String userId) {
        
        StudyPlan studyPlan = studyPlanService.generateStudyPlan(syllabusId, startDate, endDate, busyHours, userId);
        return ResponseEntity.ok(studyPlan);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudyPlan>> getUserStudyPlans(@PathVariable String userId) {
        List<StudyPlan> studyPlans = studyPlanService.getUserStudyPlans(userId);
        return ResponseEntity.ok(studyPlans);
    }
    
    @PutMapping("/{studyPlanId}/session/{sessionId}")
    public ResponseEntity<StudyPlan> updateSessionProgress(
            @PathVariable Long studyPlanId,
            @PathVariable Long sessionId,
            @RequestParam boolean completed) {
        
        StudyPlan updatedPlan = studyPlanService.updateSessionProgress(studyPlanId, sessionId, completed);
        return ResponseEntity.ok(updatedPlan);
    }
} 