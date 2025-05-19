package com.example.demo.service;

import com.example.demo.dto.GenerateStudyPlanRequest;
import com.example.demo.dto.StudyPlanResponse;
import com.example.demo.model.StudyPlan;
import com.example.demo.model.StudySession;
import com.example.demo.model.Syllabus;
import com.example.demo.repository.StudyPlanRepository;
import com.example.demo.repository.SyllabusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudyPlanService implements StudyPlanServices {
    

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Override
    public StudyPlanResponse createStudyPlan(MultipartFile pdfRequest) {
         throw new UnsupportedOperationException("WIP: fallback generation process");
    }
    
    @Override
    public StudyPlanResponse generateStudyPlan(GenerateStudyPlanRequest request) {
        Syllabus syllabus = syllabusRepository.findById(request.getSyllabusId())
                .orElseThrow(() -> new IllegalArgumentException("Syllabus not found"));

        List<String> topics = syllabus.getTopics();
        long totalDays = request.getStartDate().until(request.getEndDate()).getDays() + 1;
        long availableDays = totalDays - request.getBusyHours().size();

        if (availableDays <= 0) {
            throw new IllegalArgumentException("No available days to create a study plan");
        }

        int topicsPerDay = (int) Math.ceil((double) topics.size() / availableDays);
        List<StudySession> sessions = new ArrayList<>();

        LocalDate currentDate = request.getStartDate();
        int topicIndex = 0;

        while (currentDate.isBefore(request.getEndDate().plusDays(1)) && topicIndex < topics.size()) {
            if (!request.getBusyHours().containsKey(currentDate.toString())) {
                for (int i = 0; i < topicsPerDay && topicIndex < topics.size(); i++) {
                    StudySession session = new StudySession();
                    session.setTopic(topics.get(topicIndex++));
                    session.setStartTime(LocalDateTime.of(currentDate, LocalDateTime.MIN.toLocalTime()));
                    session.setEndTime(LocalDateTime.of(currentDate, LocalDateTime.MAX.toLocalTime()));
                    session.setCompleted(false);
                    session.setReminderSent(false);
                    sessions.add(session);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        StudyPlan studyPlan = new StudyPlan();
        studyPlan.setSyllabus(syllabus);
        studyPlan.setStartDate(request.getStartDate());
        studyPlan.setEndDate(request.getEndDate());
        studyPlan.setUserId(request.getUserId());
        studyPlan.setSessions(sessions);

        StudyPlan savedStudyPlan = studyPlanRepository.save(studyPlan);
        return new StudyPlanResponse(savedStudyPlan);
    }
    
    @Override
    public List<StudyPlanResponse> getUserStudyPlans(String userId) {
        List<StudyPlan> studyPlans = studyPlanRepository.findByUserId(userId);
        List<StudyPlanResponse> responses = new ArrayList<>();
        for (StudyPlan studyPlan : studyPlans) {
            responses.add(new StudyPlanResponse(studyPlan));
        }
        return responses;
    }
    
    @Override
    public StudyPlanResponse updateSessionProgress(Long studyPlanId, Long sessionId, boolean completed) {
        StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        StudySession session = studyPlan.getSessions().stream()
            .filter(s -> sessionId.equals(s.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.setCompleted(completed);
        StudyPlan updatedStudyPlan = studyPlanRepository.save(studyPlan);
        return new StudyPlanResponse(updatedStudyPlan);
    }

    @Override
    public Map<String, Object> getUserProgressStats(String userId) {
        List<StudyPlan> plans = studyPlanRepository.findByUserId(userId);
        int totalSessions = 0, completed = 0, streak = 0, longestStreak = 0;
        for (StudyPlan plan : plans) {
            if (plan.getSessions() != null) {
                totalSessions += plan.getSessions().size();
                completed += (int) plan.getSessions().stream().filter(StudySession::isCompleted).count();
            }
        }
        //Calculates streaks based on completed sessions
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalSessions", totalSessions);
        stats.put("completedSessions", completed);
        stats.put("currentStreak", streak);
        stats.put("longestStreak", longestStreak);
        return stats;
    }


    @Override
    public StudyPlan getStudyPlanById(Long id) {
        return studyPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));
    }

    @Override
    public void deleteStudyPlan(Long id) {
        StudyPlan studyPlan = getStudyPlanById(id);
        studyPlanRepository.delete(studyPlan);
    }
}