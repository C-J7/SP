package com.example.demo.service;

import com.example.demo.model.StudyPlan;
import com.example.demo.model.StudySession;
import com.example.demo.model.Syllabus;
import com.example.demo.repository.StudyPlanRepository;
import com.example.demo.repository.SyllabusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudyPlanService {
    
    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private SyllabusRepository syllabusRepository;
    
    public StudyPlan generateStudyPlan(Long syllabusId, LocalDate startDate, LocalDate endDate, 
                                     Map<String, LocalDateTime> busyHours, String userId) {
        Syllabus syllabus = syllabusRepository.findById(syllabusId)
                .orElseThrow(() -> new IllegalArgumentException("Syllabus not found"));

        List<String> topics = syllabus.getTopics();
        long totalDays = startDate.until(endDate).getDays() + 1;
        long availableDays = totalDays - busyHours.size();

        if (availableDays <= 0) {
            throw new IllegalArgumentException("No available days to create a study plan");
        }

        int topicsPerDay = (int) Math.ceil((double) topics.size() / availableDays);
        List<StudySession> sessions = new ArrayList<>();

        LocalDate currentDate = startDate;
        int topicIndex = 0;

        while (currentDate.isBefore(endDate.plusDays(1)) && topicIndex < topics.size()) {
            if (!busyHours.containsKey(currentDate.toString())) {
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
        studyPlan.setStartDate(startDate);
        studyPlan.setEndDate(endDate);
        studyPlan.setUserId(userId);
        studyPlan.setSessions(sessions);

        return studyPlanRepository.save(studyPlan);
    }
    
    public List<StudyPlan> getUserStudyPlans(String userId) {
        return studyPlanRepository.findByUserId(userId);
    }
    
    public StudyPlan updateSessionProgress(Long studyPlanId, Long sessionId, boolean completed) {
        StudyPlan studyPlan = studyPlanRepository.findById(studyPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Study plan not found"));

        StudySession session = studyPlan.getSessions().stream()
            .filter(s -> sessionId.equals(s.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.setCompleted(completed);
        return studyPlanRepository.save(studyPlan);
    }
}