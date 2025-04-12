package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "syllabi")
public class Syllabus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String faculty;
    private String department;
    private String semester;
    private String fileUrl;
    private LocalDateTime uploadDate;
    
    @ElementCollection
    @CollectionTable(name = "syllabus_topics", joinColumns = @JoinColumn(name = "syllabus_id"))
    @Column(name = "topic")
    private List<String> topics;
    
    private String uploadedBy;
} 