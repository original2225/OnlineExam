package com.example.entity;

import java.time.LocalDateTime;

public class AchievementRecord {
    private Integer id;
    private Integer userId;
    private String userName;
    private String userRole;
    private String achievementKey;
    private String achievementName;
    private String description;
    private String icon;
    private String category;
    private Integer score;
    private LocalDateTime earnedAt;
    private Boolean notified;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAchievementKey() { return achievementKey; }
    public void setAchievementKey(String achievementKey) { this.achievementKey = achievementKey; }

    public String getAchievementName() { return achievementName; }
    public void setAchievementName(String achievementName) { this.achievementName = achievementName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }

    public Boolean getNotified() { return notified; }
    public void setNotified(Boolean notified) { this.notified = notified; }
}
