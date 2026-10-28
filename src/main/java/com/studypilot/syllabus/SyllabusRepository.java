package com.studypilot.syllabus;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {

    @EntityGraph(attributePaths = "topics")
    List<Syllabus> findByUploadedByOrderByUploadedAtDesc(String uploadedBy);

    /** Null filters are ignored, so one query serves the catalogue browse and the search box. */
    @EntityGraph(attributePaths = "topics")
    @Query("""
            select distinct s from Syllabus s
            where (:faculty is null or s.faculty = :faculty)
              and (:department is null or s.department = :department)
              and (:semester is null or s.semester = :semester)
            order by s.uploadedAt desc
            """)
    List<Syllabus> findCatalogue(@Param("faculty") String faculty, @Param("department") String department,
            @Param("semester") String semester);
}
