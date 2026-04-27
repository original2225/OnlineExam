package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GradingSubmission {

    private Integer id;
    private Integer recordId;
    private Integer graderId;
    private String graderName;
    private String graderRole;
    private BigDecimal manualScore;
    private BigDecimal totalScore;
    private BigDecimal performanceScore;
    private String advisoryVote;
    private String rejectionReasons;
    private String customReason;
    private String comment;
    private LocalDateTime createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getGraderId() {
        return graderId;
    }

    public void setGraderId(Integer graderId) {
        this.graderId = graderId;
    }

    public String getGraderName() {
        return graderName;
    }

    public void setGraderName(String graderName) {
        this.graderName = graderName;
    }

    public String getGraderRole() {
        return graderRole;
    }

    public void setGraderRole(String graderRole) {
        this.graderRole = graderRole;
    }

    public BigDecimal getManualScore() {
        return manualScore;
    }

    public void setManualScore(BigDecimal manualScore) {
        this.manualScore = manualScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(BigDecimal performanceScore) {
        this.performanceScore = performanceScore;
    }

    public String getAdvisoryVote() {
        return advisoryVote;
    }

    public void setAdvisoryVote(String advisoryVote) {
        this.advisoryVote = advisoryVote;
    }

    public String getRejectionReasons() {
        return rejectionReasons;
    }

    public void setRejectionReasons(String rejectionReasons) {
        this.rejectionReasons = rejectionReasons;
    }

    public String getCustomReason() {
        return customReason;
    }

    public void setCustomReason(String customReason) {
        this.customReason = customReason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
