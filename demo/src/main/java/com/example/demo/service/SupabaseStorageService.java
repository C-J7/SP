package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    private final HttpClient httpClient;

    public SupabaseStorageService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String uploadFile(MultipartFile file, String filename) throws IOException {
        String storageUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filename;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", file.getContentType())
                .header("x-upsert", "true") // Update if exists
                .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new IOException("Failed to upload file to Supabase. Status code: " + response.statusCode());
            }

            // Return the public URL for the uploaded file
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filename;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    public void deleteFile(String filename) throws IOException {
        String storageUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filename;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl))
                .header("Authorization", "Bearer " + supabaseKey)
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200 && response.statusCode() != 404) {
                throw new IOException("Failed to delete file from Supabase. Status code: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Delete operation interrupted", e);
        }
    }
} 