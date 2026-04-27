package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.ForumLike;
import com.example.service.ForumCommentService;
import com.example.service.ForumLikeService;
import com.example.service.ForumPostService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛点赞/收藏接口
 */
@RestController
@RequestMapping("/forumLike")
public class ForumLikeController {

    @Resource
    private ForumLikeService forumLikeService;

    @Resource
    private ForumPostService forumPostService;

    @Resource
    private ForumCommentService forumCommentService;

    /** 切换点赞/收藏 */
    @PostMapping("/toggle")
    public Result toggle(@RequestBody ForumLike like) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        if (like.getTargetType() == null || like.getTargetId() == null || like.getType() == null) {
            return Result.error("参数不完整");
        }
        boolean liked = forumLikeService.toggle(
                currentUser.getId(), currentUser.getRole(),
                like.getTargetType(), like.getTargetId(), like.getType());
        // 更新对应目标的计数
        if ("post".equals(like.getTargetType())) {
            forumPostService.updateLikeCount(like.getTargetId(), liked ? 1 : -1);
        } else if ("comment".equals(like.getTargetType())) {
            forumCommentService.updateLikeCount(like.getTargetId(), liked ? 1 : -1);
        }
        return Result.success(liked);
    }

    /** 检查是否已点赞/收藏 */
    @GetMapping("/check")
    public Result check(@RequestParam String targetType,
                        @RequestParam Integer targetId,
                        @RequestParam String type) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.success(false);
        boolean checked = forumLikeService.check(
                currentUser.getId(), currentUser.getRole(), targetType, targetId, type);
        return Result.success(checked);
    }

    /** 获取我的收藏列表 */
    @GetMapping("/favorites")
    public Result favorites() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        List<ForumLike> list = forumLikeService.getFavorites(currentUser.getId(), currentUser.getRole());
        return Result.success(list);
    }
}
