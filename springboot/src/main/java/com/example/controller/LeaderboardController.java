package com.example.controller;

import com.example.common.Result;
import com.example.mapper.ExamRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @Resource
    private ExamRecordMapper examRecordMapper;

    @GetMapping("/entryReview")
    public Result entryReview() {
        return Result.success(examRecordMapper.selectEntryReviewLeaderboard());
    }
}
