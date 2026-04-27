package com.example.entity;

import java.time.LocalDateTime;

public class PollVote {
    private Integer id;
    private Integer pollId;
    private Integer optionId;
    private Integer userId;
    private String userRole;
    private LocalDateTime createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPollId() { return pollId; }
    public void setPollId(Integer pollId) { this.pollId = pollId; }
    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
