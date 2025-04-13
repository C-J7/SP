package com.example.demo.controller;

import com.example.demo.model.Pdf;
import com.example.demo.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/upload")
    public ResponseEntity<Pdf> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        Pdf pdf = pdfService.uploadPdf(file);
        return ResponseEntity.ok(pdf);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pdf> getPdf(@PathVariable String id) {
        Pdf pdf = pdfService.getPdf(id);
        return ResponseEntity.ok(pdf);
    }
} 