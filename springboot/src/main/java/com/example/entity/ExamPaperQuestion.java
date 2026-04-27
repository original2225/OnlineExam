package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExamPaperQuestion {

    /** 主键ID */
    private Integer id;
    /** 试卷ID */
    private Integer paperId;
    /** 题目ID */
    private Integer questionId;
    /** 题目序号 */
    private Integer questionOrder;
    /** 本题分值 */
    private BigDecimal score;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 题目信息（关联查询用，非数据库字段） */
    private Question question;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
