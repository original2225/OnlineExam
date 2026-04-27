package com.example.service;

import com.example.entity.ForumComment;
import com.example.mapper.ForumCommentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForumCommentService {

    @Resource
    private ForumCommentMapper forumCommentMapper;

    public void add(ForumComment comment) {
        if (comment.getStatus() == null) comment.setStatus("active");
        if (comment.getLikeCount() == null) comment.setLikeCount(0);
        forumCommentMapper.insert(comment);
    }

    public void deleteById(Integer id) {
        forumCommentMapper.deleteById(id);
    }

    public ForumComment selectById(Integer id) {
        return forumCommentMapper.selectById(id);
    }

    /**
     * 获取帖子的树形评论
     */
    public List<ForumComment> getTreeComments(Integer postId) {
        List<ForumComment> all = forumCommentMapper.selectByPostId(postId);
        return buildTree(all);
    }

    private List<ForumComment> buildTree(List<ForumComment> all) {
        List<ForumComment> roots = new ArrayList<>();
        for (ForumComment c : all) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                c.setChildren(new ArrayList<>());
                roots.add(c);
            }
        }
        for (ForumComment c : all) {
            if (c.getParentId() != null && c.getParentId() != 0) {
                for (ForumComment root : roots) {
                    if (root.getId().equals(c.getParentId())) {
                        root.getChildren().add(c);
                        break;
                    }
                }
            }
        }
        return roots;
    }

    public void updateLikeCount(Integer id, int delta) {
        forumCommentMapper.updateLikeCount(id, delta);
    }
}
