package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

    private static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String GOOGLE_REDIRECT_URI = System.getenv("GOOGLE_REDIRECT_URI");

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        String redirectUri = "https://accounts.google.com/o/oauth2/auth?client_id=" + GOOGLE_CLIENT_ID +
                             "&redirect_uri=" + GOOGLE_REDIRECT_URI +
                             "&response_type=code&scope=https://www.googleapis.com/auth/calendar";
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUri).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        //Exchange auth code for access token
        //Use the token to interact with the Google Calendar API
        return ResponseEntity.ok("Google OAuth successful! Code: " + code);
    }
}