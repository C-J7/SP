package com.example.demo.repository;

import com.example.demo.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findByUserId(String userId);
    List<StudyPlan> findBySyllabusId(Long syllabusId);
} 