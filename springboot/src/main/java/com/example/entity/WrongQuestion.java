package com.example.entity;

import java.time.LocalDateTime;

public class WrongQuestion {

    /** 主键ID */
    private Integer id;
    /** 用户ID */
    private Integer userId;
    /** 用户角色 */
    private String userRole;
    /** 题目ID */
    private Integer questionId;
    /** 来源: exam/practice/manual */
    private String source;
    /** 来源ID (考试记录ID或练习ID) */
    private Integer sourceId;
    /** 用户的错误答案 */
    private String wrongAnswer;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public String getWrongAnswer() {
        return wrongAnswer;
    }

    public void setWrongAnswer(String wrongAnswer) {
        this.wrongAnswer = wrongAnswer;
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
