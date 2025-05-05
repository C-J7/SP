package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Configuration
public class DatabaseConnectionDebugger {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionDebugger.class);
    
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Bean
    public CommandLineRunner debugDatabaseConnection() {
        return args -> {
            log.info("DEBUGGING DATABASE CONNECTION");
            log.info("============================");
            log.info("Database URL: {}", maskPassword(url));
            log.info("Database Username: {}", username);
            log.info("Current Host Info: {}", InetAddress.getLocalHost().toString());
            log.info("JVM DNS Cache Policy: {}", System.getProperty("networkaddress.cache.ttl", "Not Set"));
            
            // Test direct connection using DriverManager
            try {
                Class.forName("org.postgresql.Driver");
                log.info("PostgreSQL Driver loaded successfully");
                
                Properties props = new Properties();
                props.setProperty("user", username);
                props.setProperty("password", password);
                props.setProperty("connectTimeout", "10");
                props.setProperty("loginTimeout", "10");
                
                log.info("Attempting direct JDBC connection...");
                Connection conn = DriverManager.getConnection(url, props);
                log.info("✓ Direct JDBC connection successful!");
                log.info("Database Product Name: {}", conn.getMetaData().getDatabaseProductName());
                log.info("Database Product Version: {}", conn.getMetaData().getDatabaseProductVersion());
                conn.close();
            } catch (Exception e) {
                log.error("✗ Direct JDBC connection failed!", e);
                log.error("Error Message: {}", e.getMessage());
                if (e.getCause() != null) {
                    log.error("Root Cause: {}", e.getCause().getMessage());
                }
                
                // Try alternative connection methods
                String altUrl = url;
                if (url.contains("db.oyyhqjpemqltsqjlxyqn.supabase.co")) {
                    log.info("Trying to resolve Supabase host IP...");
                    try {
                        InetAddress[] addresses = InetAddress.getAllByName("db.oyyhqjpemqltsqjlxyqn.supabase.co");
                        for (InetAddress addr : addresses) {
                            log.info("Resolved IP: {}", addr.getHostAddress());
                        }
                        
                        // Try direct IP connection
                        if (addresses.length > 0) {
                            String ipAddress = addresses[0].getHostAddress();
                            altUrl = url.replace("db.oyyhqjpemqltsqjlxyqn.supabase.co", ipAddress);
                            log.info("Attempting connection to: {}", maskPassword(altUrl));
                            try {
                                Connection conn = DriverManager.getConnection(altUrl, username, password);
                                log.info("✓ Direct IP connection successful!");
                                conn.close();
                            } catch (Exception e2) {
                                log.error("✗ Direct IP connection failed: {}", e2.getMessage());
                            }
                        }
                    } catch (Exception e3) {
                        log.error("Could not resolve host: {}", e3.getMessage());
                    }
                }
            }
        };
    }
    
    private String maskPassword(String url) {
        // Don't log actual passwords
        if (url != null && url.contains("password=")) {
            return url.replaceAll("password=([^&]*)", "password=******");
        }
        return url;
    }
}