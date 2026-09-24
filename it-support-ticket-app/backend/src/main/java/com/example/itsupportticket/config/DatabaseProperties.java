package com.example.itsupportticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "it.support.ticket.database")
public class DatabaseProperties {

    private String path = "./data/it-support-ticket.db";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
