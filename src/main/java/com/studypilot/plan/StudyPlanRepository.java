package com.studypilot.plan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    @EntityGraph(attributePaths = {"sessions", "syllabus"})
    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = {"sessions", "syllabus"})
    Optional<StudyPlan> findWithSessionsById(Long id);

    @Query("select s from StudySession s join fetch s.plan p join fetch p.syllabus where p.userId = :userId")
    List<StudySession> findSessionsByUser(@Param("userId") String userId);

    @Query("""
            select s from StudySession s join fetch s.plan p join fetch p.syllabus
            where p.userId = :userId and s.completed = false and s.endTime >= :now
            order by s.startTime asc
            """)
    List<StudySession> findUpcoming(@Param("userId") String userId, @Param("now") LocalDateTime now, Pageable page);
}
