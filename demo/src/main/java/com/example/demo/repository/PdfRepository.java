package com.example.demo.repository;

import com.example.demo.model.Pdf;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfRepository extends JpaRepository<Pdf, Long> {
    //Custom query methods

    //to find a PDF by its name:
    Optional<Pdf> findByName(String name);

    //to find a PDF by its storage path:
    Optional<Pdf> findByStoragePath(String storagePath);

    //to find a PDF by its URL:
    Optional<Pdf> findByUrl(String url);

    //to find a PDF by its ID:
    Optional<Pdf> findById(Integer id);

    
}