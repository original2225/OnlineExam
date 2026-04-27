package com.example.controller;

import com.example.common.Result;
import com.example.entity.StudyCheckin;
import com.example.mapper.StudyCheckinMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/studyCheckin")
public class StudyCheckinController {

    @Resource
    private StudyCheckinMapper studyCheckinMapper;

    @PostMapping("/checkin")
    public Result checkin(@RequestBody Map<String, Object> params) {
        Integer userId = ((Number) params.get("userId")).intValue();
        String userRole = (String) params.get("userRole");
        LocalDate today = LocalDate.now();

        StudyCheckin existing = studyCheckinMapper.selectByUserAndDate(userId, userRole, today);
        if (existing != null) {
            return Result.error("今日已打卡");
        }

        int streakDays = 1;
        StudyCheckin latest = studyCheckinMapper.selectLatestByUser(userId, userRole);
        if (latest != null && latest.getCheckinDate().plusDays(1).equals(today)) {
            streakDays = latest.getStreakDays() + 1;
        }

        StudyCheckin checkin = new StudyCheckin();
        checkin.setUserId(userId);
        checkin.setUserRole(userRole);
        checkin.setCheckinDate(today);
        checkin.setStreakDays(streakDays);
        studyCheckinMapper.insert(checkin);

        Map<String, Object> result = new HashMap<>();
        result.put("streakDays", streakDays);
        result.put("checkinDate", today.toString());
        return Result.success(result);
    }

    @GetMapping("/status")
    public Result status(@RequestParam Integer userId, @RequestParam String userRole) {
        LocalDate today = LocalDate.now();
        StudyCheckin todayCheckin = studyCheckinMapper.selectByUserAndDate(userId, userRole, today);
        StudyCheckin latest = studyCheckinMapper.selectLatestByUser(userId, userRole);
        int totalDays = studyCheckinMapper.countByUser(userId, userRole);

        Map<String, Object> result = new HashMap<>();
        result.put("todayChecked", todayCheckin != null);
        result.put("streakDays", latest != null ? latest.getStreakDays() : 0);
        result.put("totalDays", totalDays);
        result.put("latestDate", latest != null ? latest.getCheckinDate().toString() : null);
        return Result.success(result);
    }

    @GetMapping("/leaderboard")
    public Result leaderboard() {
        List<StudyCheckin> list = studyCheckinMapper.selectLeaderboard();
        return Result.success(list);
    }
}
