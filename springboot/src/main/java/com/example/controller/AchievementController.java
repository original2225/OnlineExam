package com.example.controller;

import com.example.common.Result;
import com.example.entity.AchievementRecord;
import com.example.service.AchievementService;
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
        Integer userId = ((Number) params.get("userId")).intValue();
        String userName = (String) params.get("userName");
        String userRole = (String) params.get("userRole");
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
        return Result.success(achievementService.getUserAchievements(userId, userRole));
    }

    /**
     * 获取所有成就（含已解锁/未解锁状态）
     */
    @GetMapping("/all")
    public Result allAchievements(@RequestParam(required = false) Integer userId, @RequestParam(required = false) String userRole) {
        return Result.success(achievementService.getAllAchievements(userId, userRole));
    }

    /**
     * 获取成就统计
     */
    @GetMapping("/stats")
    public Result stats(@RequestParam(required = false) Integer userId, @RequestParam(required = false) String userRole) {
        return Result.success(achievementService.getStats(userId, userRole));
    }
}
