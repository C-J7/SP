package com.studypilot.syllabus;

import java.time.Instant;
import java.util.List;

public record SyllabusResponse(Long id, String title, String faculty, String department, String semester,
        String originalFilename, String sha256, String uploadedBy, Instant uploadedAt, List<String> topics) {

    static SyllabusResponse from(Syllabus s) {
        return new SyllabusResponse(s.getId(), s.getTitle(), s.getFaculty(), s.getDepartment(), s.getSemester(),
                s.getOriginalFilename(), s.getSha256(), s.getUploadedBy(), s.getUploadedAt(), List.copyOf(s.getTopics()));
    }
}
