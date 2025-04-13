package com.example.demo.service;

import com.example.demo.model.Pdf;
import com.example.demo.repository.PdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class PdfService {

    @Autowired
    private PdfRepository pdfRepository;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    public Pdf uploadPdf(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        // Create new PDF entity
        Pdf pdf = new Pdf();
        pdf.setId(UUID.randomUUID().toString());
        pdf.setName(file.getOriginalFilename());
        pdf.setSize((int) file.getSize());
        pdf.setUploadedAt(ZonedDateTime.now());

        // Upload to Supabase storage
        String storagePath = "pdfs/" + pdf.getId() + ".pdf";
        String fileUrl = supabaseStorageService.uploadFile(file, storagePath);
        pdf.setStoragePath(storagePath);

        // Extract and store content (if needed)
        // Note: You might want to use PDFBox here to extract text content
        pdf.setContent(""); // For now, leaving it empty

        // Save to database
        return pdfRepository.save(pdf);
    }

    public Pdf getPdf(String id) {
        return pdfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PDF not found with id: " + id));
    }
} 