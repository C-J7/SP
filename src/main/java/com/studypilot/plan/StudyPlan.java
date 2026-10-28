package com.studypilot.plan;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.studypilot.syllabus.Syllabus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;

    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int hoursPerDay;
    private Instant createdAt;
    private Instant updatedAt;

    /** Optimistic lock: exposed to clients as the ETag so concurrent edits are detected, not silently lost. */
    @Version
    private long version;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime asc")
    private List<StudySession> sessions = new ArrayList<>();

    protected StudyPlan() {
    }

    public StudyPlan(Syllabus syllabus, String userId, LocalDate startDate, LocalDate endDate, int hoursPerDay,
            Instant createdAt) {
        this.syllabus = syllabus;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hoursPerDay = hoursPerDay;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /** Session edits change the plan, so they must bump its version and trip concurrent writers. */
    public void touch(Instant at) {
        this.updatedAt = at;
    }

    public void addSession(StudySession session) {
        session.attachTo(this);
        sessions.add(session);
    }

    public Long getId() { return id; }
    public Syllabus getSyllabus() { return syllabus; }
    public String getUserId() { return userId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getHoursPerDay() { return hoursPerDay; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public List<StudySession> getSessions() { return sessions; }
}
