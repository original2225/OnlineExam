package com.example.entity;

import java.time.LocalDateTime;

public class ExamRecordChatRead {
    private Integer id;
    private Integer recordId;
    private Integer userId;
    private String userRole;
    private Integer lastReadMessageId;
    private LocalDateTime lastReadAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public Integer getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Integer lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}
