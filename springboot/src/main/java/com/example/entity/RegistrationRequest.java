package com.example.entity;

import java.time.LocalDateTime;

/**
 * 注册申请实体类
 */
public class RegistrationRequest {

    /** 主键ID */
    private Integer id;

    /** 用户名 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 姓名 */
    private String name;

    /** 学号 */
    private String studentNo;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 分组 */
    private String className;
    /** 分支 */
    private String branch;

    /** 申请状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝 */
    private String status;

    /** 申请时间 */
    private LocalDateTime requestTime;

    /** 审批时间 */
    private LocalDateTime approvalTime;

    /** 审批人ID */
    private Integer approvedBy;

    /** 拒绝原因 */
    private String rejectionReason;

    /** 审批Token（用于邮件链接验证） */
    private String approvalToken;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getApprovalToken() {
        return approvalToken;
    }

    public void setApprovalToken(String approvalToken) {
        this.approvalToken = approvalToken;
    }
}
