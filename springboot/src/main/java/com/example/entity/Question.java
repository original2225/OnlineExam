package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Question {

    /** 主键ID */
    private Integer id;
    /** 分类ID */
    private Integer categoryId;
    /** 分类名称（关联查询用） */
    private String categoryName;
    /** 题型: single/multiple/judge/essay/fillin */
    private String type;
    /** 题目内容 */
    private String content;
    /** 选项JSON */
    private Map<String, String> options;
    /** 答案 */
    private String answer;
    /** 答案解析 */
    private String analysis;
    /** 题目图片列表JSON: [{"url":"...", "caption":"..."}] */
    private List<Map<String, String>> images;
    /** 难度: easy/medium/hard */
    private String difficulty;
    /** 分值 */
    private BigDecimal score;
    /** 创建人ID */
    private Integer createdBy;
    /** 创建人姓名（关联查询用） */
    private String createdByName;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 记录状态 */
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public List<Map<String, String>> getImages() {
        return images;
    }

    public void setImages(List<Map<String, String>> images) {
        this.images = images;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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

    /**
     * 判断是否为客观题（单选/判断/多选）
     */
    public boolean isObjective() {
        return "single".equals(type) || "multiple".equals(type) || "judge".equals(type);
    }

    /**
     * 判断是否为需要人工批改的题型
     */
    public boolean needsManualGrading() {
        return "essay".equals(type) || "fill".equals(type) || "fillin".equals(type);
    }

    /**
     * 获取题型显示名称
     */
    public String getTypeName() {
        if ("single".equals(type)) return "单选题";
        if ("multiple".equals(type)) return "多选题";
        if ("judge".equals(type)) return "判断题";
        if ("essay".equals(type)) return "简答题";
        if ("fill".equals(type)) return "填空题";
        if ("fillin".equals(type)) return "填空题";
        return type;
    }

    /**
     * 获取难度显示名称
     */
    public String getDifficultyName() {
        if ("easy".equals(difficulty)) return "简单";
        if ("medium".equals(difficulty)) return "中等";
        if ("hard".equals(difficulty)) return "困难";
        return difficulty;
    }
}
