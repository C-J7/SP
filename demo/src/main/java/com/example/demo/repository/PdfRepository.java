package com.example.demo.repository;

import com.example.demo.model.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfRepository extends JpaRepository<Pdf, Long> {
    // Custom query methods can be defined here if needed
}