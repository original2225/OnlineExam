package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamRecord;
import com.example.service.ExamRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 成绩前端请求接口
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 分页查询成绩
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) Integer examId,
                              @RequestParam(required = false) Integer studentId,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        PageInfo<ExamRecord> pageInfo = examRecordService.selectPage(record, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取考试成绩统计
     */
    @GetMapping("/getStatistics/{examId}")
    public Result getStatistics(@PathVariable Integer examId) {
        List<ExamRecord> records = examRecordService.getByExamId(examId);

        int totalCount = records.size();
        int passCount = 0;
        int failCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = new BigDecimal("1000");

        for (ExamRecord record : records) {
            if (record.getIsPass() != null && record.getIsPass()) {
                passCount++;
            } else {
                failCount++;
            }
            if (record.getTotalScore() != null) {
                totalScore = totalScore.add(record.getTotalScore());
                if (record.getTotalScore().compareTo(maxScore) > 0) {
                    maxScore = record.getTotalScore();
                }
                if (record.getTotalScore().compareTo(minScore) < 0) {
                    minScore = record.getTotalScore();
                }
            }
        }

        BigDecimal avgScore = totalCount > 0
                ? totalScore.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (minScore.compareTo(new BigDecimal("1000")) >= 0) {
            minScore = BigDecimal.ZERO;
        }

        BigDecimal passRate = totalCount > 0
                ? BigDecimal.valueOf(passCount * 100).divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> statistics = Map.of(
            "totalCount", totalCount,
            "passCount", passCount,
            "failCount", failCount,
            "passRate", passRate,
            "avgScore", avgScore,
            "maxScore", maxScore,
            "minScore", minScore
        );

        return Result.success(statistics);
    }

    /**
     * 获取学生成绩列表
     */
    @GetMapping("/getByStudentId/{studentId}")
    public Result getByStudentId(@PathVariable Integer studentId) {
        List<ExamRecord> list = examRecordService.getByStudentId(studentId);
        return Result.success(list);
    }
}
