package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExamPaper {

    /** 主键ID */
    private Integer id;
    /** 试卷名称 */
    private String name;
    /** 试卷描述 */
    private String description;
    /** 总分 */
    private BigDecimal totalScore;
    /** 考试时���(分钟) */
    private Integer totalTime;
    /** 及格分 */
    private BigDecimal passScore;
    /** 试卷类型: manual/random */
    private String type;
    /** 随机组卷配置 */
    private Map<String, Object> randomConfig;
    /** 创建人ID */
    private Integer createdBy;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 记录状态 */
    private String status;
    /** 试卷题目列表（非数据库字段） */
    private List<Question> questions;
    /** 题目数量（非数据库字段） */
    private Integer questionCount;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public BigDecimal getPassScore() {
        return passScore;
    }

    public void setPassScore(BigDecimal passScore) {
        this.passScore = passScore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getRandomConfig() {
        return randomConfig;
    }

    public void setRandomConfig(Map<String, Object> randomConfig) {
        this.randomConfig = randomConfig;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
