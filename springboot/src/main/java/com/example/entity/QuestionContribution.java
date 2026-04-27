package com.example.entity;

import java.time.LocalDateTime;

public class QuestionContribution {
    private Integer id;
    private Integer userId;
    private String userName;
    private String userRole;
    private Integer categoryId;
    private String type;
    private String content;
    private String options;
    private String answer;
    private String analysis;
    private String difficulty;
    private String status;
    private Integer reviewerId;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private Integer approvedQuestionId;
    private String categoryName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getApprovedQuestionId() { return approvedQuestionId; }
    public void setApprovedQuestionId(Integer approvedQuestionId) { this.approvedQuestionId = approvedQuestionId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
