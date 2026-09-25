package com.example.itsupportticket.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, DemoDataProperties.class})
public class ItSupportTicketConfiguration implements WebMvcConfigurer {

    private final DatabaseProperties databaseProperties;

    public ItSupportTicketConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @PostConstruct
    public void initializeDatabaseDirectory() throws IOException {
        Path databasePath = Paths.get(databaseProperties.getPath());
        Path parentDirectory = databasePath.toAbsolutePath().getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://192.168.1.31:5173",
                        "http://192.168.1.*:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
