package com.example.itsupportticket.config;

import java.io.File;
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

     /**
     * Tự động kiểm tra và tạo thư mục chứa file database nếu chưa tồn tại
     */
    private void ensureDirectoryExists(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    System.out.println(">>> [SQLite] Đã tự động tạo thư mục chứa DB: " + parentDir.getAbsolutePath());
                }
            }
        }
    }
}
