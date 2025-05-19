package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SyllabusResponse {
    private Long id;
    private String title;
    private String faculty;
    private String department;
    private String semester;
    private String fileUrl;
    private LocalDateTime uploadDate;
    private List<String> topics;
    private String uploadedBy;
    
    //getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getFaculty() {
        return faculty;
    }
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getSemester() {
        return semester;
    }
    public void setSemester(String semester) {
        this.semester = semester;
    }
    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
    public List<String> getTopics() {
        return topics;
    }
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
    public String getUploadedBy() {
        return uploadedBy;
    }
    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    
}
