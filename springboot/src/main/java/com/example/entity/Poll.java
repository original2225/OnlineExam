package com.example.entity;

import java.time.LocalDateTime;

public class Poll {
    private Integer id;
    private String title;
    private String description;
    private String coverUrl;
    private Integer creatorId;
    private String creatorName;
    private String creatorRole;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer maxChoices;
    private Boolean isAnonymous;
    private Integer totalVotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private java.util.List<PollOption> options;
    private Boolean hasVoted;
    private java.util.List<Integer> votedOptionIds;
    private Boolean isExpired;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMaxChoices() { return maxChoices; }
    public void setMaxChoices(Integer maxChoices) { this.maxChoices = maxChoices; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public java.util.List<PollOption> getOptions() { return options; }
    public void setOptions(java.util.List<PollOption> options) { this.options = options; }
    public Boolean getHasVoted() { return hasVoted; }
    public void setHasVoted(Boolean hasVoted) { this.hasVoted = hasVoted; }
    public java.util.List<Integer> getVotedOptionIds() { return votedOptionIds; }
    public void setVotedOptionIds(java.util.List<Integer> votedOptionIds) { this.votedOptionIds = votedOptionIds; }
    public Boolean getIsExpired() {
        if (endTime != null) {
            return LocalDateTime.now().isAfter(endTime);
        }
        return false;
    }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }
}
