package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.ForumComment;
import com.example.service.ForumCommentService;
import com.example.service.ForumPostService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛评论接口
 */
@RestController
@RequestMapping("/forumComment")
public class ForumCommentController {

    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private ForumPostService forumPostService;

    /** 发表评论 */
    @PostMapping("/add")
    public Result add(@RequestBody ForumComment comment) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        // 检查帖子是否被锁定
        if (comment.getPostId() != null) {
            var post = forumPostService.selectById(comment.getPostId());
            if (post != null && Boolean.TRUE.equals(post.getIsLocked())) {
                return Result.error("帖子已锁定，无法评论");
            }
        }
        comment.setAuthorId(currentUser.getId());
        comment.setAuthorName(currentUser.getUsername());
        comment.setAuthorRole(currentUser.getRole());
        forumCommentService.add(comment);
        if (comment.getPostId() != null) {
            forumPostService.incrementCommentCount(comment.getPostId());
        }
        return Result.success();
    }

    /** 删除评论（本人或管理员） */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        ForumComment db = forumCommentService.selectById(id);
        if (db == null) return Result.error("评论不存在");
        // 只有评论作者或管理员可删除
        if (!db.getAuthorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            return Result.error("无权操作");
        }
        forumCommentService.deleteById(id);
        // 递减帖子评论数
        if (db.getPostId() != null) {
            forumPostService.decrementCommentCount(db.getPostId());
        }
        return Result.success();
    }

    /** 获取帖子评论（树形） */
    @GetMapping("/tree/{postId}")
    public Result tree(@PathVariable Integer postId) {
        List<ForumComment> tree = forumCommentService.getTreeComments(postId);
        return Result.success(tree);
    }

    private boolean isAdmin(Account user) {
        String role = user.getRole();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "HELPER".equals(role);
    }
}
