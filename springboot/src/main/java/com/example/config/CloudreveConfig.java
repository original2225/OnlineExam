package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloudreve")
public class CloudreveConfig {

    private String baseUrl;
    private String username;
    private String password;
    private String uploadDir;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
}
