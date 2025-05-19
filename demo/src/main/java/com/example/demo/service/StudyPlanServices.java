package com.example.demo.service;

import com.example.demo.dto.GenerateStudyPlanRequest;
import com.example.demo.dto.StudyPlanResponse;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.model.StudyPlan;

import java.util.List;
import java.util.Map;

public interface StudyPlanServices {
    StudyPlanResponse createStudyPlan(MultipartFile pdfRequest);
    
    //Generates a study plan based on the request
    //Multiple process as fallbacks. 
    List<StudyPlanResponse> getUserStudyPlans(String userId);

    StudyPlan getStudyPlanById(Long id);

    StudyPlanResponse generateStudyPlan(GenerateStudyPlanRequest request);

    StudyPlanResponse updateSessionProgress(Long studyPlanId, Long sessionId, boolean completed);
    Map<String, Object> getUserProgressStats(String userId);

    void deleteStudyPlan(Long id);
}
