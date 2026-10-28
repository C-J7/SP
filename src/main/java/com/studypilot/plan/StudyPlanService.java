package com.studypilot.plan;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import com.studypilot.audit.AuditService;
import com.studypilot.common.NotFoundException;
import com.studypilot.syllabus.Syllabus;
import com.studypilot.syllabus.SyllabusRepository;

@Service
public class StudyPlanService {

    private final StudyPlanRepository plans;
    private final SyllabusRepository syllabi;
    private final AuditService audit;
    private final Clock clock;

    public StudyPlanService(StudyPlanRepository plans, SyllabusRepository syllabi, AuditService audit, Clock clock) {
        this.plans = plans;
        this.syllabi = syllabi;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public StudyPlanResponse generate(GenerateStudyPlanRequest request) {
        Syllabus syllabus = syllabi.findById(request.syllabusId())
                .orElseThrow(() -> new NotFoundException("Syllabus", request.syllabusId()));
        List<StudySession> sessions = SessionScheduler.schedule(syllabus.getTopics(), request.startDate(),
                request.endDate(), request.busyHoursOrEmpty(), request.hoursPerDayOrDefault(), request.preferredStart());

        StudyPlan plan = new StudyPlan(syllabus, request.userId(), request.startDate(), request.endDate(),
                request.hoursPerDayOrDefault(), clock.instant());
        sessions.forEach(plan::addSession);
        StudyPlan saved = plans.saveAndFlush(plan);
        audit.record(request.userId(), "STUDY_PLAN_GENERATED", "StudyPlan", saved.getId(),
                sessions.size() + " sessions for syllabus " + syllabus.getId());
        return StudyPlanResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StudyPlanResponse get(Long id) {
        return StudyPlanResponse.from(load(id));
    }

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> forUser(String userId) {
        return plans.findByUserIdOrderByCreatedAtDesc(userId).stream().map(StudyPlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UpcomingSession> upcoming(String userId, int limit) {
        return plans.findUpcoming(userId, LocalDateTime.now(clock), PageRequest.of(0, limit)).stream()
                .map(UpcomingSession::from)
                .toList();
    }

    /**
     * @param expectedVersion the version from the client's ETag; null skips the precondition check
     */
    @Transactional
    public StudyPlanResponse setSessionCompleted(Long planId, Long sessionId, boolean completed, Long expectedVersion) {
        StudyPlan plan = load(planId);
        if (expectedVersion != null && expectedVersion != plan.getVersion()) {
            throw new ErrorResponseException(HttpStatus.PRECONDITION_FAILED, ProblemDetail.forStatusAndDetail(
                    HttpStatus.PRECONDITION_FAILED,
                    "Plan is at version " + plan.getVersion() + ", not " + expectedVersion + ". Re-read and retry."), null);
        }
        StudySession session = plan.getSessions().stream()
                .filter(s -> s.getId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("StudySession", sessionId));
        if (session.isCompleted() != completed) {
            session.markCompleted(completed, clock.instant());
            plan.touch(clock.instant());
            audit.record(plan.getUserId(), completed ? "SESSION_COMPLETED" : "SESSION_REOPENED", "StudySession",
                    sessionId, session.getTopic());
        }
        plans.flush(); // run the versioned UPDATE now so a lost race fails here and the response carries the new ETag
        return StudyPlanResponse.from(plan);
    }

    @Transactional
    public void delete(Long id) {
        StudyPlan plan = load(id);
        plans.delete(plan);
        audit.record(plan.getUserId(), "STUDY_PLAN_DELETED", "StudyPlan", id, null);
    }

    private StudyPlan load(Long id) {
        return plans.findWithSessionsById(id).orElseThrow(() -> new NotFoundException("StudyPlan", id));
    }

    public record UpcomingSession(Long id, Long studyPlanId, String topic, String course, LocalDateTime startTime,
            LocalDateTime endTime) {

        static UpcomingSession from(StudySession s) {
            return new UpcomingSession(s.getId(), s.getPlan().getId(), s.getTopic(),
                    s.getPlan().getSyllabus().getTitle(), s.getStartTime(), s.getEndTime());
        }
    }
}
