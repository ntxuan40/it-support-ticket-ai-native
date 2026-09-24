package com.example.itsupportticket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, DemoDataProperties.class})
public class ItSupportTicketConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "it.support.ticket.demo-data", name = "enabled", havingValue = "true")
    public String demoDataBootMessage() {
        return "Demo-data initialization is configured; backend implementation is intentionally deferred.";
    }
}
