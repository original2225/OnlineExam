package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.ExamAnswer;
import com.example.entity.ExamRecord;
import com.example.entity.ExamPaper;
import com.example.entity.Examiner;
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
import java.util.List;
import java.util.Map;

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
                              @RequestBody Map<String, Object> params,
                              @RequestParam(required = false) Integer gradedBy) {
        Account currentUser = TokenUtils.getCurrentUser();
        Integer graderId = currentUser != null ? currentUser.getId() : gradedBy;
        String graderName = getName(currentUser);
        String graderRole = currentUser != null ? currentUser.getRole() : "UNKNOWN";

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answerRows = (List<Map<String, Object>>) params.get("answers");
        List<ExamAnswer> answers = answerRows == null ? List.of() : answerRows.stream().map(row -> {
            ExamAnswer answer = new ExamAnswer();
            answer.setId(numberToInteger(row.get("id")));
            answer.setScore(numberToBigDecimal(row.get("score")));
            answer.setComment((String) row.get("comment"));
            return answer;
        }).toList();

        examAnswerService.batchUpdateForGrading(answers, graderId);

        BigDecimal manualScore = BigDecimal.ZERO;
        for (ExamAnswer answer : answers) {
            if (answer.getScore() != null) {
                manualScore = manualScore.add(answer.getScore());
            }
        }

        ExamRecord record = examRecordService.getDetail(recordId);
        BigDecimal autoScore = record != null && record.getAutoScore() != null ? record.getAutoScore() : BigDecimal.ZERO;
        BigDecimal totalScore = autoScore.add(manualScore);

        GradingSubmission submission = new GradingSubmission();
        submission.setRecordId(recordId);
        submission.setGraderId(graderId);
        submission.setGraderName(graderName);
        submission.setGraderRole(graderRole);
        submission.setManualScore(manualScore);
        submission.setTotalScore(totalScore);
        submission.setPerformanceScore(numberToBigDecimal(params.get("performanceScore")));
        submission.setAdvisoryVote((String) params.get("advisoryVote"));
        submission.setRejectionReasons((String) params.get("rejectionReasons"));
        submission.setCustomReason((String) params.get("customReason"));
        submission.setComment((String) params.get("comment"));
        gradingSubmissionMapper.insert(submission);

        recalculateFinalScore(recordId);
        ensureChiefExaminer(recordId, graderId, graderRole, graderName);

        return Result.success();
    }

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
            List<BigDecimal> scores = submissions.stream()
                    .map(GradingSubmission::getManualScore)
                    .sorted()
                    .toList();
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
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper != null && paper.getPassScore() != null) {
            record.setIsPass(finalTotal.compareTo(paper.getPassScore()) >= 0);
        }
        if (record.getExamStatus() == null || "PENDING".equals(record.getExamStatus())) {
            record.setExamStatus("UNDER_REVIEW");
        }
        examRecordService.updateById(record);
    }

    private void ensureChiefExaminer(Integer recordId, Integer graderId, String graderRole, String graderName) {
        ExamRecord record = examRecordService.getDetail(recordId);
        if (record == null || record.getChiefExaminerId() != null) return;
        ExamRecord update = new ExamRecord();
        update.setId(recordId);
        update.setChiefExaminerId(graderId);
        update.setChiefExaminerRole(graderRole);
        update.setChiefExaminerName(graderName);
        examRecordService.updateById(update);
    }

    /**
     * 获取待批阅数量
     */
    @GetMapping("/countPending")
    public Result countPending() {
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

    private Integer numberToInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private BigDecimal numberToBigDecimal(Object value) {
        return value instanceof Number number ? new BigDecimal(String.valueOf(number)) : null;
    }

    private String getName(Account account) {
        if (account instanceof Admin admin) return admin.getName();
        if (account instanceof Examiner examiner) return examiner.getName();
        return account != null ? account.getUsername() : "unknown";
    }
}
