package com.example.entity;

import java.time.LocalDateTime;

public class Exam {

    /** 主键ID */
    private Integer id;
    /** 考试名称 */
    private String name;
    /** 试卷ID */
    private Integer paperId;
    /** 试卷名称（关联查询用） */
    private String paperName;
    /** 考试描述 */
    private String description;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 考试时长(分钟) */
    private Integer duration;
    /** 是否允许迟到 */
    private Boolean allowLate;
    /** 迟到时间(分钟) */
    private Integer lateTime;
    /** 创建人ID */
    private Integer createdBy;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 状态: draft/published/ongoing/finished/cancelled */
    private String status;
    /** 分支 */
    private String branch;
    /** 考试类型: scheduled(统一考试)/permanent(常驻考试) */
    private String examType;
    /** 是否自动公示成绩 */
    private Boolean autoPublish;
    /** 是否开启录屏 */
    private Boolean enableRecording;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getAllowLate() {
        return allowLate;
    }

    public void setAllowLate(Boolean allowLate) {
        this.allowLate = allowLate;
    }

    public Integer getLateTime() {
        return lateTime;
    }

    public void setLateTime(Integer lateTime) {
        this.lateTime = lateTime;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    /** 未批阅数量（非数据库字段） */
    private Integer ungradedCount;

    public Integer getUngradedCount() {
        return ungradedCount;
    }

    public void setUngradedCount(Integer ungradedCount) {
        this.ungradedCount = ungradedCount;
    }

    public Boolean getAutoPublish() {
        return autoPublish;
    }

    public void setAutoPublish(Boolean autoPublish) {
        this.autoPublish = autoPublish;
    }

    public Boolean getEnableRecording() {
        return enableRecording;
    }

    public void setEnableRecording(Boolean enableRecording) {
        this.enableRecording = enableRecording;
    }
}
