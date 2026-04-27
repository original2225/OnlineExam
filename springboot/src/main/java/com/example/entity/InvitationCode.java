package com.example.entity;

import java.time.LocalDateTime;

/**
 * 邀请码实体类
 */
public class InvitationCode {

    /** 主键ID */
    private Integer id;

    /** 邀请码（唯一） */
    private String code;

    /** 状态：UNUSED-未使用，USED-已使用，EXPIRED-已过期 */
    private String status;

    /** 使用用户ID */
    private Integer usedBy;

    /** 使用用户名 */
    private String usedByUsername;

    /** 使用时间 */
    private LocalDateTime usedTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 创建者ID（管理员） */
    private Integer createdBy;

    /** 过期时间（可选，不设置则永不过期） */
    private LocalDateTime expireTime;

    /** 备注 */
    private String remark;

    /** 目标角色: STUDENT 或 EXAMINER */
    private String targetRole;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(Integer usedBy) {
        this.usedBy = usedBy;
    }

    public String getUsedByUsername() {
        return usedByUsername;
    }

    public void setUsedByUsername(String usedByUsername) {
        this.usedByUsername = usedByUsername;
    }

    public LocalDateTime getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(LocalDateTime usedTime) {
        this.usedTime = usedTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
