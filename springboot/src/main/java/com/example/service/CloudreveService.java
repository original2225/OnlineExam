package com.example.service;

import com.example.config.CloudreveConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudreveService {

    private static final Logger log = LoggerFactory.getLogger(CloudreveService.class);

    @Resource
    private CloudreveConfig config;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    private String accessToken;
    private long tokenExpiresAt = 0;

    /**
     * 确保有有效的 access token
     */
    private synchronized String ensureToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return accessToken;
        }
        try {
            String url = config.getBaseUrl() + "/api/v4/session/token/password";
            Map<String, String> body = new HashMap<>();
            body.put("email", config.getUsername());
            body.put("password", config.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt() == 0) {
                JsonNode tokenNode = root.path("data").path("token");
                accessToken = tokenNode.path("access_token").asText();
                String expiresStr = tokenNode.path("access_expires").asText();
                // 提前5分钟过期
                tokenExpiresAt = LocalDateTime.parse(expiresStr.substring(0, expiresStr.indexOf('.')))
                        .minusMinutes(5).toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
                return accessToken;
            }
            log.error("Cloudreve login failed: {}", response.getBody());
        } catch (Exception e) {
            log.error("Cloudreve auth error", e);
        }
        return null;
    }

    /**
     * 上传文件到 Cloudreve 并返回分享链接
     */
    public String uploadAndShare(MultipartFile file, String fileName) {
        String token = ensureToken();
        if (token == null) {
            log.error("Cannot get Cloudreve token");
            return null;
        }

        try {
            // Step 1: 创建上传会话
            String sessionId = createUploadSession(token, fileName, file.getSize());
            if (sessionId == null) return null;

            // Step 2: 上传文件内容（小文件直接上传）
            boolean uploaded = uploadFileContent(token, sessionId, file, fileName);
            if (!uploaded) return null;

            // Step 3: 创建分享链接
            return createShareLink(token, fileName);
        } catch (Exception e) {
            log.error("Cloudreve upload error", e);
            return null;
        }
    }

    private String createUploadSession(String token, String fileName, long fileSize) {
        try {
            String url = config.getBaseUrl() + "/api/v4/file/upload/session";
            Map<String, Object> body = new HashMap<>();
            body.put("name", fileName);
            body.put("size", fileSize);
            body.put("dir", config.getUploadDir());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt() == 0) {
                return root.path("data").path("session_id").asText();
            }
            log.error("Create upload session failed: {}", response.getBody());
        } catch (Exception e) {
            log.error("Create upload session error", e);
        }
        return null;
    }

    private boolean uploadFileContent(String token, String sessionId, MultipartFile file, String fileName) {
        try {
            String url = config.getBaseUrl() + "/api/v4/file/upload/chunk";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(token);

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("file", file.getResource());
            formData.add("session_id", sessionId);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(formData, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("code").asInt() == 0;
        } catch (Exception e) {
            log.error("Upload chunk error", e);
            return false;
        }
    }

    private String createShareLink(String token, String fileName) {
        try {
            // 先列出文件获取 source ID
            String listUrl = config.getBaseUrl() + "/api/v4/file/list?dir=" + config.getUploadDir();
            HttpHeaders listHeaders = new HttpHeaders();
            listHeaders.setBearerAuth(token);
            HttpEntity<Void> listEntity = new HttpEntity<>(listHeaders);
            ResponseEntity<String> listResponse = restTemplate.exchange(listUrl, HttpMethod.GET, listEntity, String.class);
            JsonNode listRoot = objectMapper.readTree(listResponse.getBody());

            String sourceId = null;
            JsonNode items = listRoot.path("data").path("items");
            for (JsonNode item : items) {
                if (fileName.equals(item.path("name").asText())) {
                    sourceId = item.path("id").asText();
                    break;
                }
            }
            if (sourceId == null) return null;

            // 创建分享链接
            String shareUrl = config.getBaseUrl() + "/api/v4/share";
            Map<String, Object> shareBody = new HashMap<>();
            shareBody.put("source_id", sourceId);
            shareBody.put("type", "file");

            HttpHeaders shareHeaders = new HttpHeaders();
            shareHeaders.setContentType(MediaType.APPLICATION_JSON);
            shareHeaders.setBearerAuth(token);
            HttpEntity<String> shareEntity = new HttpEntity<>(objectMapper.writeValueAsString(shareBody), shareHeaders);

            ResponseEntity<String> shareResponse = restTemplate.exchange(shareUrl, HttpMethod.POST, shareEntity, String.class);
            JsonNode shareRoot = objectMapper.readTree(shareResponse.getBody());
            if (shareRoot.path("code").asInt() == 0) {
                String key = shareRoot.path("data").path("key").asText();
                return config.getBaseUrl() + "/s/" + key;
            }
        } catch (Exception e) {
            log.error("Create share link error", e);
        }
        return null;
    }
}
