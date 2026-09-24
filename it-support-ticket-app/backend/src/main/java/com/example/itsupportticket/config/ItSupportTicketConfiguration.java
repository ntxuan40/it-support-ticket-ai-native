package com.example.itsupportticket.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, DemoDataProperties.class})
public class ItSupportTicketConfiguration {

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

    @Bean
    @ConditionalOnProperty(prefix = "it.support.ticket.demo-data", name = "enabled", havingValue = "true")
    public String demoDataBootMessage() {
        return "Demo-data initialization is configured; backend implementation is intentionally deferred.";
    }
}
