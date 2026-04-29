package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.ExamAnswer;
import com.example.entity.ExamRecord;
import com.example.service.ExamRecordService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * 考试记录前端请求接口
 */
@RestController
@RequestMapping("/examRecord")
public class ExamRecordController {

    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 开始考试
     */
    @PostMapping("/start")
    public Result start(@RequestParam Integer examId, @RequestParam Integer studentId) {
        if (!isCurrentStudent(studentId)) {
            return forbidden();
        }
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
        if (!isCurrentStudentRecord(recordId)) {
            return forbidden();
        }
        examRecordService.saveAnswer(recordId, questionId, studentAnswer);
        return Result.success();
    }

    /**
     * 批量保存答案
     */
    @PostMapping("/saveAnswers")
    public Result saveAnswers(@RequestParam Integer recordId, @RequestBody List<ExamAnswer> answers) {
        if (!isCurrentStudentRecord(recordId)) {
            return forbidden();
        }
        examRecordService.batchSaveAnswers(recordId, answers);
        return Result.success();
    }

    /**
     * 提交考试
     */
    @PostMapping("/submit")
    public Result submit(@RequestParam Integer recordId) {
        if (!isCurrentStudentRecord(recordId)) {
            return forbidden();
        }
        ExamRecord record = examRecordService.submitExam(recordId);
        return Result.success(record);
    }

    /**
     * 获取考试记录详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        if (!canAccessRecord(id)) {
            return forbidden();
        }
        ExamRecord record = examRecordService.getDetail(id);
        return Result.success(record);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(ExamRecord record) {
        if (!hasReviewPermission()) {
            return forbidden();
        }
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
        if (!hasReviewPermission()) {
            return forbidden();
        }
        PageInfo<ExamRecord> pageInfo = examRecordService.selectPage(record, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取玩家的考试记录
     */
    @GetMapping("/getByStudentId/{studentId}")
    public Result getByStudentId(@PathVariable Integer studentId) {
        if (!canAccessStudent(studentId)) {
            return forbidden();
        }
        List<ExamRecord> list = examRecordService.getByStudentId(studentId);
        return Result.success(list);
    }

    /**
     * 获取考试的所有记录
     */
    @GetMapping("/getByExamId/{examId}")
    public Result getByExamId(@PathVariable Integer examId) {
        if (!hasReviewPermission()) {
            return forbidden();
        }
        List<ExamRecord> list = examRecordService.getByExamId(examId);
        return Result.success(list);
    }

    /**
     * 更新考试记录
     */
    @PutMapping("/update")
    public Result update(@RequestBody ExamRecord record) {
        if (!hasReviewPermission()) {
            return forbidden();
        }
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
        if (!isCurrentStudentRecord(recordId)) {
            return forbidden();
        }
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
        if (!canAccessStudent(studentId)) {
            return forbidden();
        }
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
        if (!hasReviewPermission()) {
            return forbidden();
        }
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
        if (!canAccessStudent(studentId)) {
            return forbidden();
        }
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

    private boolean canAccessRecord(Integer recordId) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return false;
        }
        if (REVIEW_ROLES.contains(current.getRole())) {
            return true;
        }
        ExamRecord record = examRecordService.getDetail(recordId);
        return RoleEnum.USER.name().equals(current.getRole()) && record != null && current.getId().equals(record.getStudentId());
    }

    private boolean isCurrentStudentRecord(Integer recordId) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !RoleEnum.USER.name().equals(current.getRole())) {
            return false;
        }
        ExamRecord record = examRecordService.getDetail(recordId);
        return record != null && current.getId().equals(record.getStudentId());
    }

    private boolean canAccessStudent(Integer studentId) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return false;
        }
        return REVIEW_ROLES.contains(current.getRole())
                || (RoleEnum.USER.name().equals(current.getRole()) && current.getId().equals(studentId));
    }

    private boolean isCurrentStudent(Integer studentId) {
        Account current = TokenUtils.getCurrentUser();
        return current != null && RoleEnum.USER.name().equals(current.getRole()) && current.getId().equals(studentId);
    }

    private boolean hasReviewPermission() {
        Account current = TokenUtils.getCurrentUser();
        return current != null && REVIEW_ROLES.contains(current.getRole());
    }

    private Result forbidden() {
        return Result.error("403", "无权限访问");
    }
}
