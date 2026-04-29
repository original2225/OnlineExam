package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.AchievementRecord;
import com.example.service.AchievementService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/achievement")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    /**
     * 解锁成就
     */
    @PostMapping("/unlock")
    public Result unlock(@RequestBody Map<String, Object> params) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        Integer userId = current.getId();
        String userName = current.getUsername();
        String userRole = current.getRole();
        String achievementKey = (String) params.get("achievementKey");

        AchievementRecord unlocked = achievementService.tryUnlock(userId, userName, userRole, achievementKey);
        if (unlocked == null) {
            return Result.success("已有此成就");
        }
        return Result.success(unlocked);
    }

    /**
     * 获取用户成就列表
     */
    @GetMapping("/my")
    public Result myAchievements(@RequestParam Integer userId, @RequestParam String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (!matchesCurrent(current, userId, userRole)) {
            return forbidden();
        }
        return Result.success(achievementService.getUserAchievements(current.getId(), current.getRole()));
    }

    /**
     * 获取所有成就（含已解锁/未解锁状态）
     */
    @GetMapping("/all")
    public Result allAchievements(@RequestParam(required = false) Integer userId, @RequestParam(required = false) String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (userId != null || userRole != null) {
            if (!matchesCurrent(current, userId, userRole)) {
                return forbidden();
            }
            return Result.success(achievementService.getAllAchievements(current.getId(), current.getRole()));
        }
        return Result.success(achievementService.getAllAchievements(null, null));
    }

    /**
     * 获取成就统计
     */
    @GetMapping("/stats")
    public Result stats(@RequestParam(required = false) Integer userId, @RequestParam(required = false) String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (userId != null || userRole != null) {
            if (!matchesCurrent(current, userId, userRole)) {
                return forbidden();
            }
            return Result.success(achievementService.getStats(current.getId(), current.getRole()));
        }
        return Result.success(achievementService.getStats(null, null));
    }

    private boolean matchesCurrent(Account current, Integer userId, String userRole) {
        return current != null && current.getId().equals(userId) && current.getRole().equals(userRole);
    }

    private Result forbidden() {
        return Result.error("403", "无权限访问");
    }
}
