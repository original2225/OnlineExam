package com.example.entity;

import java.time.LocalDateTime;

public class ExamPermission {

    /** 主键ID */
    private Integer id;
    /** 考试ID */
    private Integer examId;
    /** 玩家ID */
    private Integer studentId;
    /** 玩家姓名（关联查询用） */
    private String studentName;
    /** 玩家编号（关联查询用） */
    private String studentNo;
    /** 创建时间 */
    private LocalDateTime createdAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
