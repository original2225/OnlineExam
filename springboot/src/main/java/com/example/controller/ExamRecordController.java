package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamAnswer;
import com.example.entity.ExamRecord;
import com.example.service.ExamRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 考试记录前端请求接口
 */
@RestController
@RequestMapping("/examRecord")
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 开始考试
     */
    @PostMapping("/start")
    public Result start(@RequestParam Integer examId, @RequestParam Integer studentId) {
        ExamRecord record = examRecordService.startExam(examId, studentId);
        return Result.success(record);
    }

    /**
     * 保存答案
     */
    @PostMapping("/saveAnswer")
    public Result saveAnswer(@RequestParam Integer recordId,
                             @RequestParam Integer questionId,
                             @RequestParam String studentAnswer) {
        examRecordService.saveAnswer(recordId, questionId, studentAnswer);
        return Result.success();
    }

    /**
     * 批量保存答案
     */
    @PostMapping("/saveAnswers")
    public Result saveAnswers(@RequestParam Integer recordId, @RequestBody List<ExamAnswer> answers) {
        examRecordService.batchSaveAnswers(recordId, answers);
        return Result.success();
    }

    /**
     * 提交考试
     */
    @PostMapping("/submit")
    public Result submit(@RequestParam Integer recordId) {
        ExamRecord record = examRecordService.submitExam(recordId);
        return Result.success(record);
    }

    /**
     * 获取考试记录详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        ExamRecord record = examRecordService.getDetail(id);
        return Result.success(record);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(ExamRecord record) {
        List<ExamRecord> list = examRecordService.selectAll(record);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(ExamRecord record,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ExamRecord> pageInfo = examRecordService.selectPage(record, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取玩家的考试记录
     */
    @GetMapping("/getByStudentId/{studentId}")
    public Result getByStudentId(@PathVariable Integer studentId) {
        List<ExamRecord> list = examRecordService.getByStudentId(studentId);
        return Result.success(list);
    }

    /**
     * 获取考试的所有记录
     */
    @GetMapping("/getByExamId/{examId}")
    public Result getByExamId(@PathVariable Integer examId) {
        List<ExamRecord> list = examRecordService.getByExamId(examId);
        return Result.success(list);
    }

    /**
     * 更新考试记录
     */
    @PutMapping("/update")
    public Result update(@RequestBody ExamRecord record) {
        examRecordService.updateById(record);
        return Result.success();
    }

    /**
     * 获取考试通过公示列表（不含分数）
     */
    @GetMapping("/publicResults")
    public Result publicResults() {
        List<ExamRecord> list = examRecordService.getPublicResults();
        return Result.success(list);
    }

    /**
     * 报告切屏（防作弊）
     */
    @PostMapping("/reportSwitch")
    public Result reportSwitch(@RequestParam Integer recordId) {
        ExamRecord record = examRecordService.getDetail(recordId);
        if (record == null) return Result.error("记录不存在");
        int current = record.getSwitchCount() != null ? record.getSwitchCount() : 0;
        current++;
        ExamRecord update = new ExamRecord();
        update.setId(recordId);
        update.setSwitchCount(current);
        examRecordService.updateById(update);
        Map<String, Object> result = new HashMap<>();
        result.put("switchCount", current);
        result.put("maxReached", current >= 5);
        return Result.success(result);
    }

    /**
     * 获取成绩对比数据（击败百分比）
     */
    @GetMapping("/compareScore")
    public Result compareScore(@RequestParam Integer examId, @RequestParam Integer studentId) {
        List<ExamRecord> allRecords = examRecordService.getByExamId(examId).stream()
                .filter(r -> "completed".equals(r.getStatus()))
                .toList();

        if (allRecords.isEmpty()) {
            return Result.success(Map.of("avgScore", 0, "maxScore", 0, "percentile", 0));
        }

        double avgScore = allRecords.stream()
                .mapToDouble(r -> r.getTotalScore() != null ? r.getTotalScore().doubleValue() : 0)
                .average().orElse(0);

        double maxScore = allRecords.stream()
                .mapToDouble(r -> r.getTotalScore() != null ? r.getTotalScore().doubleValue() : 0)
                .max().orElse(0);

        double myScore = allRecords.stream()
                .filter(r -> studentId.equals(r.getStudentId()))
                .findFirst()
                .map(r -> r.getTotalScore() != null ? r.getTotalScore().doubleValue() : 0)
                .orElse(0.0);

        long belowMe = allRecords.stream()
                .filter(r -> (r.getTotalScore() != null ? r.getTotalScore().doubleValue() : 0) < myScore)
                .count();

        int percentile = allRecords.size() > 1 ? (int) Math.round((double) belowMe / allRecords.size() * 100) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("avgScore", Math.round(avgScore * 10) / 10.0);
        result.put("maxScore", (int) maxScore);
        result.put("percentile", percentile);
        return Result.success(result);
    }

    @GetMapping("/gradingStats")
    public Result gradingStats() {
        List<ExamRecord> all = examRecordService.selectAll(new ExamRecord());
        long totalCount = all.size();
        long gradedCount = all.stream().filter(r -> "completed".equals(r.getStatus()) && r.getTotalScore() != null).count();
        long pendingCount = all.stream().filter(r -> "completed".equals(r.getStatus()) && r.getTotalScore() == null).count();

        java.time.LocalDateTime monthStart = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long gradedThisMonth = all.stream()
                .filter(r -> r.getSubmitTime() != null && r.getSubmitTime().isAfter(monthStart) && r.getTotalScore() != null)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmissions", totalCount);
        stats.put("gradedCount", gradedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("gradedThisMonth", gradedThisMonth);
        return Result.success(stats);
    }

    @GetMapping("/studentStats/{studentId}")
    public Result studentStats(@PathVariable Integer studentId) {
        List<ExamRecord> records = examRecordService.getByStudentId(studentId);
        long completed = records.stream().filter(r -> "completed".equals(r.getStatus())).count();
        long passed = records.stream().filter(r -> Boolean.TRUE.equals(r.getIsPass())).count();

        java.time.LocalDateTime monthStart = java.time.LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long thisMonth = records.stream().filter(r -> r.getSubmitTime() != null && r.getSubmitTime().isAfter(monthStart)).count();

        double avgScore = records.stream()
                .filter(r -> r.getTotalScore() != null && "completed".equals(r.getStatus()))
                .mapToDouble(r -> r.getTotalScore().doubleValue())
                .average().orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExams", records.size());
        stats.put("completedExams", completed);
        stats.put("passedExams", passed);
        stats.put("thisMonthExams", thisMonth);
        stats.put("avgScore", Math.round(avgScore * 10) / 10.0);
        return Result.success(stats);
    }
}
