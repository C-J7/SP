package com.example.demo.service;

import com.example.demo.model.Syllabus;
import com.example.demo.repository.SyllabusRepository;
import com.example.demo.exception.InvalidPdfException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SyllabusService {
    
    @Autowired
    private SyllabusRepository syllabusRepository;
    
    @Autowired
    private PdfParsingService pdfParsingService;
    
    @Autowired
    private SupabaseStorageService supabaseStorageService;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    public Syllabus uploadSyllabus(MultipartFile file, String title, String faculty, 
                                 String department, String semester, String userId) throws IOException {
        // Validate input parameters
        validateInputParameters(title, faculty, department, semester, userId);
        
        // Validate PDF file
        if (!pdfParsingService.isValidPdf(file)) {
            throw new InvalidPdfException("The uploaded file is not a valid PDF");
        }
        
        // Extract text and topics from PDF
        String pdfContent = pdfParsingService.extractText(file);
        List<String> topics = pdfParsingService.extractTopics(pdfContent);
        
        if (topics.isEmpty()) {
            throw new InvalidPdfException("No topics could be extracted from the PDF");
        }
        
        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Upload file to Supabase storage
        String fileUrl;
        try {
            fileUrl = supabaseStorageService.uploadFile(file, newFilename);
        } catch (IOException e) {
            throw new IOException("Failed to upload file to storage: " + e.getMessage(), e);
        }
        
        // Create and save syllabus
        Syllabus syllabus = new Syllabus();
        syllabus.setTitle(title);
        syllabus.setFaculty(faculty);
        syllabus.setDepartment(department);
        syllabus.setSemester(semester);
        syllabus.setUploadedBy(userId);
        syllabus.setFileUrl(fileUrl);
        syllabus.setUploadDate(LocalDateTime.now());
        syllabus.setTopics(topics);
        
        return syllabusRepository.save(syllabus);
    }
    
    public List<Syllabus> getSyllabi(String faculty, String department, String semester) {
        if (faculty == null && department == null && semester == null) {
            return syllabusRepository.findAll();
        }
        return syllabusRepository.findByFacultyAndDepartmentAndSemester(faculty, department, semester);
    }
    
    public List<Syllabus> getUserSyllabi(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return syllabusRepository.findByUploadedBy(userId);
    }
    
    private void validateInputParameters(String title, String faculty, String department, 
                                      String semester, String userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (faculty == null || faculty.trim().isEmpty()) {
            throw new IllegalArgumentException("Faculty cannot be null or empty");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be null or empty");
        }
        if (semester == null || semester.trim().isEmpty()) {
            throw new IllegalArgumentException("Semester cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }
    
    // TODO: Implement Supabase storage upload
    private String uploadToSupabase(MultipartFile file, String filename) {
        // Implementation for uploading to Supabase storage
        return null;
    }
} 