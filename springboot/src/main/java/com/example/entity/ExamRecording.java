package com.example.entity;

import java.time.LocalDateTime;

public class ExamRecording {

    private Integer id;
    private Integer examId;
    private Integer studentId;
    private Integer recordId;
    private String fileUrl;
    private Long fileSize;
    /** 录屏时长(秒) */
    private Integer duration;
    /** 切屏次数 */
    private Integer switchCount;
    /** 状态: recording/uploaded/failed */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 考试名称（非数据库字段） */
    private String examName;
    /** 学生姓名（非数据库字段） */
    private String studentName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getSwitchCount() { return switchCount; }
    public void setSwitchCount(Integer switchCount) { this.switchCount = switchCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
