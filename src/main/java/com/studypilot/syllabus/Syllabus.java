package com.studypilot.syllabus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "syllabi")
public class Syllabus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String faculty;
    private String department;
    private String semester;
    private String originalFilename;
    private String sha256;
    private String uploadedBy;
    private Instant uploadedAt;

    @ElementCollection
    @CollectionTable(name = "syllabus_topics", joinColumns = @JoinColumn(name = "syllabus_id"))
    @OrderColumn(name = "position")
    @Column(name = "topic")
    private List<String> topics = new ArrayList<>();

    protected Syllabus() {
    }

    public Syllabus(String title, String faculty, String department, String semester, String originalFilename,
            String sha256, String uploadedBy, Instant uploadedAt, List<String> topics) {
        this.title = title;
        this.faculty = faculty;
        this.department = department;
        this.semester = semester;
        this.originalFilename = originalFilename;
        this.sha256 = sha256;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.topics = new ArrayList<>(topics);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getFaculty() { return faculty; }
    public String getDepartment() { return department; }
    public String getSemester() { return semester; }
    public String getOriginalFilename() { return originalFilename; }
    public String getSha256() { return sha256; }
    public String getUploadedBy() { return uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public List<String> getTopics() { return topics; }
}
