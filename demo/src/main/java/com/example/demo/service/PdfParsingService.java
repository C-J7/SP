package com.example.demo.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Pdf;
import com.example.demo.repository.PdfRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfParsingService {

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Autowired
    private PdfRepository pdfRepository;

    /**
     * Validates if the uploaded file is a valid PDF.
     *
     * @param file the uploaded file
     * @return true if the file is a valid PDF, false otherwise
     */
    public boolean isValidPdf(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            return !document.isEncrypted();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Extracts the full text content from a PDF file.
     *
     * @param file the uploaded PDF file
     * @return the extracted text as a String
     * @throws IOException if an error occurs during text extraction
     */
    public String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    /**
     * Extracts topics from the text content of a PDF.
     * Assumes topics are represented as bullet points or headings.
     *
     * @param pdfContent the full text content of the PDF
     * @return a list of extracted topics
     */
    public List<String> extractTopics(String pdfContent) {
        List<String> topics = new ArrayList<>();
        String[] lines = pdfContent.split("\n");

        for (String line : lines) {
            line = line.trim();
            // Assuming topics are either bullet points or headings
            if (line.startsWith("-") || line.startsWith("*") || line.matches("^[0-9]+\\.\\s.*")) {
                topics.add(line.replaceFirst("^[*-]\\s*", "").replaceFirst("^[0-9]+\\.\\s*", "").trim());
            }
        }

        return topics;
    }

    /**
     * Uploads a PDF file to Supabase storage and returns the file URL.
     *
     * @param file the uploaded PDF file
     * @return the URL of the uploaded file in Supabase storage
     * @throws IOException if an error occurs during file upload
     */
    public Pdf uploadToSupabase(MultipartFile file) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + ".pdf";
        String storagePath = "pdfs/" + uniqueFilename;
        Pdf pdf = new Pdf();
        pdf.setUrl(supabaseStorageService.uploadFile(file, storagePath));
        return pdf;
    }

    /**
     * Finds a PDF by its ID from the database.
     *
     * @param id the ID of the PDF
     * @return the PDF object
     * @throws IllegalArgumentException if the PDF is not found
     */
    public Pdf findPdfById(String id) {
        return pdfRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PDF not found with ID: " + id));
    }
}