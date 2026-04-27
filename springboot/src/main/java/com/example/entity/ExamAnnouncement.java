package com.example.entity;

import java.time.LocalDateTime;

public class ExamAnnouncement {

    private Integer id;
    private String title;
    private String content;
    /** 类型: general/exam/result/notice */
    private String type;
    private Integer priority;
    private Integer examId;
    private Integer authorId;
    private String authorName;
    /** 状态: draft/published/archived */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 关联考试名称（非数据库字段） */
    private String examName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
}
