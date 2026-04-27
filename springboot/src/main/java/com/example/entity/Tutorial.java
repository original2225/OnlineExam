package com.example.entity;

import java.time.LocalDateTime;

public class Tutorial {

    private Integer id;
    private String title;
    private String description;
    /** 类型: video / image_text / document / markdown */
    private String contentType;
    private String coverUrl;
    private String videoUrl;
    /** 文档URL (PDF/Word/PPT) */
    private String documentUrl;
    /** 图文内容(HTML) 或 Markdown 内容 */
    private String content;
    private Integer categoryId;
    /** 关联查询用 */
    private String categoryName;
    private Integer creatorId;
    private String creatorName;
    private String creatorRole;
    private Integer viewCount;
    /** 状态: active / draft */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
