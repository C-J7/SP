package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "pdfs")
public class Pdf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String name;
    
    private Integer size;  
    
    @Column(name = "storage_path")
    private String storagePath;
    
    @Column(name = "uploaded_at")
    private ZonedDateTime uploadedAt;  
    
    private String content;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}