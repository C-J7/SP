package com.example.demo.controller;

import com.example.demo.model.Syllabus;
import com.example.demo.service.SyllabusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/syllabus")
@CrossOrigin(origins = "*")
public class SyllabusController {
    
    @Autowired
    private SyllabusService syllabusService;
    
    @PostMapping("/upload")
    public ResponseEntity<Syllabus> uploadSyllabus(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("faculty") String faculty,
            @RequestParam("department") String department,
            @RequestParam("semester") String semester,
            @RequestParam("userId") String userId) {
        
        try {
            Syllabus syllabus = syllabusService.uploadSyllabus(file, title, faculty, department, semester, userId);
            return ResponseEntity.ok(syllabus);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Syllabus>> getSyllabi(
            @RequestParam(required = false) String faculty,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String semester) {
        
        List<Syllabus> syllabi = syllabusService.getSyllabi(faculty, department, semester);
        return ResponseEntity.ok(syllabi);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Syllabus>> getUserSyllabi(@PathVariable String userId) {
        List<Syllabus> syllabi = syllabusService.getUserSyllabi(userId);
        return ResponseEntity.ok(syllabi);
    }
} 