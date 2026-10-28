package com.studypilot.plan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudyPlanResponse(Long id, Long syllabusId, String syllabusTitle, String userId, LocalDate startDate,
        LocalDate endDate, int hoursPerDay, Instant createdAt, long version, int completedSessions,
        List<Session> sessions) {

    public record Session(Long id, String topic, LocalDateTime startTime, LocalDateTime endTime, boolean completed,
            Instant completedAt) {

        static Session from(StudySession s) {
            return new Session(s.getId(), s.getTopic(), s.getStartTime(), s.getEndTime(), s.isCompleted(),
                    s.getCompletedAt());
        }
    }

    static StudyPlanResponse from(StudyPlan p) {
        List<Session> sessions = p.getSessions().stream().map(Session::from).toList();
        int done = (int) sessions.stream().filter(Session::completed).count();
        return new StudyPlanResponse(p.getId(), p.getSyllabus().getId(), p.getSyllabus().getTitle(), p.getUserId(),
                p.getStartDate(), p.getEndDate(), p.getHoursPerDay(), p.getCreatedAt(), p.getVersion(), done, sessions);
    }
}
