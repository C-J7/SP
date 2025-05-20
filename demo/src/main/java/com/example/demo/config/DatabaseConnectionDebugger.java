package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.URI;
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
            log.info("🔍 DATABASE CONNECTION DIAGNOSTICS");
            log.info("=================================");
            log.info("Database URL: {}", maskPassword(url));
            log.info("Database Username: {}", username);
            log.info("Host OS & JVM: {} / Java {}", System.getProperty("os.name"), System.getProperty("java.version"));

            try {
                log.info("Current Host Info: {}", InetAddress.getLocalHost());
            } catch (Exception e) {
                log.warn("Could not determine local host info: {}", e.getMessage());
            }

            //Parse URL to show components
            try {
                URI uri = URI.create(url.replace("jdbc:", ""));
                log.info("Host: {}", uri.getHost());
                log.info("Port: {}", uri.getPort());
                log.info("Path: {}", uri.getPath());
            } catch (Exception e) {
                log.warn("Could not parse JDBC URL: {}", e.getMessage());
            }

            //Test direct connection using DriverManager
            try {
                Class.forName("org.postgresql.Driver");
                log.info("✅ PostgreSQL Driver loaded successfully");

                Properties props = new Properties();
                props.setProperty("user", username);
                props.setProperty("password", password);
                props.setProperty("ssl", "true");
                props.setProperty("sslmode", "require");
                props.setProperty("connectTimeout", "10");
                props.setProperty("loginTimeout", "10");

                log.info("⏳ Attempting direct JDBC connection...");
                Connection conn = DriverManager.getConnection(url, props);
                log.info("✅ Direct JDBC connection successful!");
                log.info("Database Product: {} {}",
                        conn.getMetaData().getDatabaseProductName(),
                        conn.getMetaData().getDatabaseProductVersion());
                conn.close();
                log.info("✅ Connection closed properly");
            } catch (Exception e) {
                log.error("❌ Direct JDBC connection failed!", e);
                log.error("Error Message: {}", e.getMessage());

                if (e.getCause() != null) {
                    log.error("Root Cause: {}", e.getCause().getMessage());
                }

                //Detailed troubleshooting instructions
                log.info("");
                log.info("🔧 TROUBLESHOOTING TIPS 🔧");
                log.info("1. Check if your username and password are correct.");
                log.info("2. Verify that your Neon database URL is correct.");
                log.info("3. Ensure that Neon allows connections from your IP.");
                log.info("4. If using SSL, make sure it's properly configured ('sslmode=require').");
                log.info("");
            }
        };
    }

    private String maskPassword(String url) {
        //Don't log actual passwords in URL parameters
        if (url == null) {
            return "null";
        }
        return url.replaceAll("password=[^&]*", "password=******")
                 .replaceAll("pwd=[^&]*", "pwd=******");
    }
}