package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamAnswer;
import com.example.entity.ExamRecord;
import com.example.entity.ExamPaper;
import com.example.entity.GradingSubmission;
import com.example.mapper.ExamPaperMapper;
import com.example.mapper.GradingSubmissionMapper;
import com.example.service.ExamAnswerService;
import com.example.service.ExamRecordService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 阅卷前端请求接口
 */
@RestController
@RequestMapping("/grading")
public class GradingController {

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private ExamAnswerService examAnswerService;

    @Resource
    private GradingSubmissionMapper gradingSubmissionMapper;

    @Resource
    private ExamPaperMapper examPaperMapper;

    /**
     * 获取待阅卷的考试记录列表
     */
    @GetMapping("/getPendingRecords/{examId}")
    public Result getPendingRecords(@PathVariable Integer examId) {
        List<ExamRecord> list = examRecordService.getByExamId(examId);
        return Result.success(list);
    }

    /**
     * 获取考试记录的详情（包含答案）
     */
    @GetMapping("/getRecordDetail/{recordId}")
    public Result getRecordDetail(@PathVariable Integer recordId) {
        ExamRecord record = examRecordService.getDetail(recordId);
        return Result.success(record);
    }

    /**
     * 获取某条记录的所有阅卷提交
     */
    @GetMapping("/getSubmissions/{recordId}")
    public Result getSubmissions(@PathVariable Integer recordId) {
        List<GradingSubmission> list = gradingSubmissionMapper.selectByRecordId(recordId);
        return Result.success(list);
    }

    /**
     * 批改简答题
     */
    @PostMapping("/gradeEssay")
    public Result gradeEssay(@RequestParam Integer answerId,
                              @RequestParam BigDecimal score,
                              @RequestParam(required = false) String comment,
                              @RequestParam Integer gradedBy) {
        ExamAnswer answer = examAnswerService.selectById(answerId);
        if (answer != null) {
            answer.setScore(score);
            answer.setComment(comment);
            answer.setGradedBy(gradedBy);
            answer.setGradedAt(LocalDateTime.now());
            examAnswerService.updateById(answer);
        }
        return Result.success();
    }

    /**
     * 批量提交批改结果（支持多阅卷人）
     * 每个阅卷人独立提交，系统记录所有提交
     * 去掉最高最低后取平均作为最终分数
     */
    @PostMapping("/batchGrade")
    public Result batchGrade(@RequestParam Integer recordId,
                              @RequestBody List<ExamAnswer> answers,
                              @RequestParam Integer gradedBy) {
        // 获取当前阅卷人信息
        com.example.entity.Account currentUser = TokenUtils.getCurrentUser();
        String graderName = currentUser != null ? currentUser.getUsername() : "unknown";
        String graderRole = currentUser != null ? currentUser.getRole() : "UNKNOWN";

        // 批量更新答案分数
        examAnswerService.batchUpdateForGrading(answers, gradedBy);

        // 计算当前阅卷人给的主观题总分
        BigDecimal manualScore = BigDecimal.ZERO;
        for (ExamAnswer answer : answers) {
            if (answer.getScore() != null) {
                manualScore = manualScore.add(answer.getScore());
            }
        }

        // 计算总分（自动分 + 手动分）
        ExamRecord record = examRecordService.getDetail(recordId);
        BigDecimal autoScore = record != null && record.getAutoScore() != null ? record.getAutoScore() : BigDecimal.ZERO;
        BigDecimal totalScore = autoScore.add(manualScore);

        // 保存本次阅卷提交
        GradingSubmission submission = new GradingSubmission();
        submission.setRecordId(recordId);
        submission.setGraderId(gradedBy);
        submission.setGraderName(graderName);
        submission.setGraderRole(graderRole);
        submission.setManualScore(manualScore);
        submission.setTotalScore(totalScore);
        gradingSubmissionMapper.insert(submission);

        // 重新计算最终分数：去掉最高最低取平均
        recalculateFinalScore(recordId);

        return Result.success();
    }

    /**
     * 重新计算最终分数：去掉最高最低取平均
     * 规则：
     * - 1个阅卷人：直接取该分数
     * - 2个阅卷人：取平均
     * - 3个及以上：去掉1个最高、1个最低，取剩余平均
     */
    private void recalculateFinalScore(Integer recordId) {
        List<GradingSubmission> submissions = gradingSubmissionMapper.selectByRecordId(recordId);
        if (submissions.isEmpty()) return;

        ExamRecord record = examRecordService.getDetail(recordId);
        if (record == null) return;

        BigDecimal autoScore = record.getAutoScore() != null ? record.getAutoScore() : BigDecimal.ZERO;

        BigDecimal finalManualScore;
        if (submissions.size() == 1) {
            finalManualScore = submissions.get(0).getManualScore();
        } else if (submissions.size() == 2) {
            BigDecimal sum = submissions.get(0).getManualScore().add(submissions.get(1).getManualScore());
            finalManualScore = sum.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            // 3个及以上：去掉最高最低
            List<BigDecimal> scores = submissions.stream()
                    .map(GradingSubmission::getManualScore)
                    .sorted()
                    .toList();
            // 去掉最低(第一个)和最高(最后一个)
            List<BigDecimal> trimmed = scores.subList(1, scores.size() - 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal s : trimmed) {
                sum = sum.add(s);
            }
            finalManualScore = sum.divide(BigDecimal.valueOf(trimmed.size()), 2, RoundingMode.HALF_UP);
        }

        BigDecimal finalTotal = autoScore.add(finalManualScore);
        record.setManualScore(finalManualScore);
        record.setTotalScore(finalTotal);
        // 取试卷的及格分作为判断标准
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper != null && paper.getPassScore() != null) {
            record.setIsPass(finalTotal.compareTo(paper.getPassScore()) >= 0);
        }
        examRecordService.updateById(record);
    }

    /**
     * 获取待批阅数量
     */
    @GetMapping("/countPending")
    public Result countPending() {
        // 统计 examStatus = PENDING 的记录数量
        com.example.entity.ExamRecord query = new com.example.entity.ExamRecord();
        query.setExamStatus("PENDING");
        List<com.example.entity.ExamRecord> records = examRecordService.selectAll(query);
        return Result.success(records.size());
    }

    /**
     * 完成阅卷
     */
    @PostMapping("/finishGrading")
    public Result finishGrading(@RequestParam Integer recordId) {
        ExamRecord record = new ExamRecord();
        record.setId(recordId);
        record.setStatus("completed");
        examRecordService.updateById(record);
        return Result.success();
    }

}
