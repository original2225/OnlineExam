package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExamRecord {

    /** 主键ID */
    private Integer id;
    /** 考试ID */
    private Integer examId;
    /** 考试名称（关联查询用） */
    private String examName;
    /** 试卷ID */
    private Integer paperId;
    /** 学生ID */
    private Integer studentId;
    /** 学生姓名（关联查询用） */
    private String studentName;
    /** 学号（关联查询用） */
    private String studentNo;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 提交时间 */
    private LocalDateTime submitTime;
    /** 实际用时(秒) */
    private Integer duration;
    /** 状态: ongoing/completed/timeout */
    private String status;
    /** 自动判分得分 */
    private BigDecimal autoScore;
    /** 人工判分得分 */
    private BigDecimal manualScore;
    /** 总分 */
    private BigDecimal totalScore;
    /** 是否及格 */
    private Boolean isPass;
    /** 考核状态: PENDING-待审阅, UNDER_REVIEW-审阅中, PASSED-考核通过, FAILED-未通过考核 */
    private String examStatus;
    /** 审批人ID */
    private Integer approvedBy;
    /** 审批人姓名（关联查询用） */
    private String approverName;
    /** 审批时间 */
    private LocalDateTime approvedAt;
    /** 是否已公示 */
    private Boolean isPublished;
    /** 公示时间 */
    private LocalDateTime publishedAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 答案列表（非数据库字段，关联查询用） */
    private List<ExamAnswer> answers;
    /** 切屏次数 */
    private Integer switchCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAutoScore() {
        return autoScore;
    }

    public void setAutoScore(BigDecimal autoScore) {
        this.autoScore = autoScore;
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

    public Boolean getIsPass() {
        return isPass;
    }

    public void setIsPass(Boolean isPass) {
        this.isPass = isPass;
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

    public String getExamStatus() {
        return examStatus;
    }

    public void setExamStatus(String examStatus) {
        this.examStatus = examStatus;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<ExamAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswer> answers) {
        this.answers = answers;
    }

    public Integer getSwitchCount() {
        return switchCount;
    }

    public void setSwitchCount(Integer switchCount) {
        this.switchCount = switchCount;
    }
}
