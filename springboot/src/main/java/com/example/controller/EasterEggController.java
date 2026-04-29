package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.EasterEggRecord;
import com.example.mapper.EasterEggRecordMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 彩蛋发现记录接口（支持多彩蛋）
 */
@RestController
@RequestMapping("/easterEgg")
public class EasterEggController {

    @Resource
    private EasterEggRecordMapper easterEggRecordMapper;

    /**
     * 记录发现彩蛋（如果还没记录过）
     */
    @PostMapping("/discover")
    public Result discover(@RequestBody Map<String, Object> params) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return Result.error("请先登录");
        }
        Integer userId = current.getId();
        String userName = current.getUsername();
        String userRole = current.getRole();
        String eggName = (String) params.getOrDefault("eggName", "logo_click");

        // 检查是否已记录
        EasterEggRecord existing = easterEggRecordMapper.selectByUserAndRoleAndEgg(userId, userRole, eggName);
        if (existing != null) {
            // 已记录，返回名次
            List<EasterEggRecord> all = easterEggRecordMapper.selectByEggName(eggName);
            int rank = 0;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getUserId().equals(userId) && all.get(i).getUserRole().equals(userRole)) {
                    rank = i + 1;
                    break;
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("rank", rank);
            result.put("total", all.size());
            result.put("alreadyDiscovered", true);
            return Result.success(result);
        }

        // 新记录
        EasterEggRecord record = new EasterEggRecord();
        record.setUserId(userId);
        record.setUserName(userName);
        record.setUserRole(userRole);
        record.setEggName(eggName);
        record.setDiscoverTime(LocalDateTime.now());
        easterEggRecordMapper.insert(record);

        // 计算名次
        int rank = easterEggRecordMapper.selectCountByEggName(eggName);
        Map<String, Object> result = new HashMap<>();
        result.put("rank", rank);
        result.put("total", rank);
        result.put("alreadyDiscovered", false);
        return Result.success(result);
    }

    /**
     * 获取排行榜（可按彩蛋名筛选）
     */
    @GetMapping("/leaderboard")
    public Result leaderboard(@RequestParam(required = false) String eggName) {
        if (eggName != null && !eggName.isEmpty()) {
            return Result.success(easterEggRecordMapper.selectByEggName(eggName));
        }
        return Result.success(easterEggRecordMapper.selectLeaderboard());
    }

    /**
     * 获取当前用户发现的所有彩蛋
     */
    @GetMapping("/myEggs")
    public Result myEggs(@RequestParam Integer userId, @RequestParam String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !current.getId().equals(userId) || !current.getRole().equals(userRole)) {
            return Result.error("403", "无权限访问");
        }
        List<EasterEggRecord> list = easterEggRecordMapper.selectAllEggsByUser(current.getId(), current.getRole());
        return Result.success(list);
    }
}
