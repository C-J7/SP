package com.studypilot.demo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.studypilot.audit.AuditService;
import com.studypilot.idempotency.IdempotencyService;
import com.studypilot.plan.SessionScheduler;
import com.studypilot.plan.StudyPlan;
import com.studypilot.plan.StudyPlanRepository;
import com.studypilot.plan.StudySession;
import com.studypilot.syllabus.Syllabus;
import com.studypilot.syllabus.SyllabusRepository;

/**
 * Gives the public demo a realistic account on every boot: two courses, plans that started two weeks ago,
 * and a completion history with a live streak and a broken one, so every dashboard widget has data.
 */
@Component
@Profile("demo")
class DemoDataSeeder implements ApplicationRunner {

    static final String DEMO_USER = "demo-user";
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SyllabusRepository syllabi;
    private final StudyPlanRepository plans;
    private final AuditService audit;
    private final Clock clock;

    DemoDataSeeder(SyllabusRepository syllabi, StudyPlanRepository plans, AuditService audit, Clock clock) {
        this.syllabi = syllabi;
        this.plans = plans;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!plans.findByUserIdOrderByCreatedAtDesc(DEMO_USER).isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now(clock);
        Syllabus dsa = syllabus("CSC 301: Data Structures and Algorithms", "Computer Science", List.of(
                "Asymptotic analysis and Big-O", "Arrays and dynamic arrays", "Linked lists", "Stacks and queues",
                "Hash tables", "Binary search trees", "Heaps and priority queues", "Graph representations",
                "Breadth-first and depth-first search", "Shortest paths: Dijkstra", "Minimum spanning trees",
                "Dynamic programming", "Greedy algorithms", "Sorting: merge and quick sort"));
        Syllabus db = syllabus("CSC 305: Database Systems", "Computer Science", List.of(
                "Relational model", "SQL joins and aggregation", "Normalisation to 3NF", "Indexing and B-trees",
                "Transactions and ACID", "Isolation levels and anomalies", "Query planning", "Replication and backups"));

        // One DSA topic a day, ticked off on these days ago: a live streak, and an older 6-day run that broke.
        Set<Integer> studiedDaysAgo = Set.of(0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12);
        seedPlan(dsa, today.minusDays(12), today.plusDays(1), LocalTime.of(18, 0), studiedDaysAgo, today);
        seedPlan(db, today.minusDays(1), today.plusDays(14), LocalTime.of(8, 0), Set.of(), today);
        log.info("Seeded demo account '{}'", DEMO_USER);
    }

    private Syllabus syllabus(String title, String department, List<String> topics) {
        Syllabus s = syllabi.save(new Syllabus(title, "Science", department, "First", "sample-syllabus.pdf",
                IdempotencyService.sha256(title), DEMO_USER, clock.instant(), topics));
        audit.record(DEMO_USER, "SYLLABUS_UPLOADED", "Syllabus", s.getId(), topics.size() + " topics (seed data)");
        return s;
    }

    private void seedPlan(Syllabus syllabus, LocalDate start, LocalDate end, LocalTime at, Set<Integer> studiedDaysAgo,
            LocalDate today) {
        StudyPlan plan = new StudyPlan(syllabus, DEMO_USER, start, end, 2, clock.instant());
        ZoneId zone = clock.getZone();
        for (StudySession s : SessionScheduler.schedule(syllabus.getTopics(), start, end, Map.of(), 2, at)) {
            int daysAgo = (int) (today.toEpochDay() - s.getStartTime().toLocalDate().toEpochDay());
            if (studiedDaysAgo.contains(daysAgo) && !s.getStartTime().isAfter(today.atTime(LocalTime.now(clock)))) {
                s.markCompleted(true, s.getEndTime().atZone(zone).toInstant());
            }
            plan.addSession(s);
        }
        StudyPlan saved = plans.save(plan);
        audit.record(DEMO_USER, "STUDY_PLAN_GENERATED", "StudyPlan", saved.getId(), "seed data");
    }
}
