package com.example.entity;

import java.time.LocalDateTime;

public class QuestionFavorite {
    private Integer id;
    private Integer userId;
    private String userRole;
    private Integer questionId;
    private LocalDateTime createdAt;
    private Question question;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
