package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.ForumPost;
import com.example.service.ForumPostService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛帖子接口
 */
@RestController
@RequestMapping("/forumPost")
public class ForumPostController {

    @Resource
    private ForumPostService forumPostService;

    /** 发帖 */
    @PostMapping("/add")
    public Result add(@RequestBody ForumPost post) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }
        post.setAuthorId(currentUser.getId());
        post.setAuthorName(currentUser.getUsername());
        post.setAuthorRole(currentUser.getRole());
        forumPostService.add(post);
        return Result.success();
    }

    /** 编辑帖子 */
    @PutMapping("/update")
    public Result update(@RequestBody ForumPost post) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        // 只能编辑自己的帖子，或者管理员可以编辑
        ForumPost db = forumPostService.selectById(post.getId());
        if (db == null) return Result.error("帖子不存在");
        if (!db.getAuthorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            return Result.error("无权操作");
        }
        forumPostService.updateById(post);
        return Result.success();
    }

    /** 删除自己的帖子 */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) return Result.error("请先登录");
        ForumPost db = forumPostService.selectById(id);
        if (db == null) return Result.error("帖子不存在");
        if (!db.getAuthorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            return Result.error("无权操作");
        }
        forumPostService.deleteById(id);
        return Result.success();
    }

    /** 管理员删除帖子 */
    @DeleteMapping("/adminDelete/{id}")
    public Result adminDelete(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) {
            return Result.error("403", "权限不足");
        }
        forumPostService.deleteById(id);
        return Result.success();
    }

    /** 管理员置顶/取消置顶 */
    @PutMapping("/toggleTop/{id}")
    public Result toggleTop(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) {
            return Result.error("403", "权限不足");
        }
        ForumPost db = forumPostService.selectById(id);
        if (db == null) return Result.error("帖子不存在");
        ForumPost update = new ForumPost();
        update.setId(id);
        update.setIsTop(db.getIsTop() == null || !db.getIsTop());
        forumPostService.updateById(update);
        return Result.success();
    }

    /** 管理员锁定/解锁帖子 */
    @PutMapping("/toggleLock/{id}")
    public Result toggleLock(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) {
            return Result.error("403", "权限不足");
        }
        ForumPost db = forumPostService.selectById(id);
        if (db == null) return Result.error("帖子不存在");
        ForumPost update = new ForumPost();
        update.setId(id);
        update.setIsLocked(db.getIsLocked() == null || !db.getIsLocked());
        forumPostService.updateById(update);
        return Result.success();
    }

    /** 分页查询帖子 */
    @GetMapping("/selectPage")
    public Result selectPage(ForumPost post,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 默认不显示已删除的
        if (post.getStatus() == null || post.getStatus().isEmpty()) {
            post.setStatus("active");
        }
        PageInfo<ForumPost> pageInfo = forumPostService.selectPage(post, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /** 帖子详情（增加浏览量） */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        forumPostService.incrementViewCount(id);
        ForumPost post = forumPostService.selectById(id);
        return Result.success(post);
    }

    /** 查询所有帖子 */
    @GetMapping("/selectAll")
    public Result selectAll(ForumPost post) {
        List<ForumPost> list = forumPostService.selectAll(post);
        return Result.success(list);
    }

    private boolean isAdmin(Account user) {
        String role = user.getRole();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "HELPER".equals(role);
    }
}
