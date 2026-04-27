package com.example.entity;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionCategory {

    /** 主键ID */
    private Integer id;
    /** 分类名称 */
    private String name;
    /** 分类描述 */
    private String description;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 记录状态 */
    private String status;
    /** 父分类ID，null表示顶级学科 */
    private Integer parentId;
    /** 图标标识 */
    private String icon;
    /** 子分类列表（非数据库字段） */
    private transient List<QuestionCategory> children;

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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<QuestionCategory> getChildren() {
        return children;
    }

    public void setChildren(List<QuestionCategory> children) {
        this.children = children;
    }
}
