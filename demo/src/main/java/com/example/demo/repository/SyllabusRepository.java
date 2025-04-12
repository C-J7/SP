package com.example.demo.repository;

import com.example.demo.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
    List<Syllabus> findByFacultyAndDepartmentAndSemester(String faculty, String department, String semester);
    List<Syllabus> findByUploadedBy(String userId);
} 