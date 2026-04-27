package com.example.entity;

import java.time.LocalDateTime;
import java.util.List;

public class ForumComment {

    private Integer id;
    private Integer postId;
    private String content;
    private Integer authorId;
    private String authorName;
    private String authorRole;
    private String authorAvatar;
    private Integer parentId;
    private Integer likeCount;
    private String status;
    private LocalDateTime createdAt;
    /** 子评论（非数据库字段） */
    private List<ForumComment> children;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ForumComment> getChildren() { return children; }
    public void setChildren(List<ForumComment> children) { this.children = children; }
}
