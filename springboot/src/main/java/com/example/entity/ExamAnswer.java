package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExamAnswer {

    /** 主键ID */
    private Integer id;
    /** 考试记录ID */
    private Integer recordId;
    /** 题目ID */
    private Integer questionId;
    /** 题目信息（关联查询用，非数据库字段） */
    private Question question;
    /** 学生答案 */
    private String studentAnswer;
    /** 是否正确 */
    private Boolean isCorrect;
    /** 得分 */
    private BigDecimal score;
    /** 评阅意见 */
    private String comment;
    /** 评阅人ID */
    private Integer gradedBy;
    /** 评阅时间 */
    private LocalDateTime gradedAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;

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

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(Integer gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
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
}
