package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.ExamRecord;
import com.example.entity.Examiner;
import com.example.entity.GradingSubmission;
import com.example.exception.CustomException;
import com.example.mapper.GradingSubmissionMapper;
import com.example.service.AdminService;
import com.example.service.ExamRecordService;
import com.example.service.ExaminerService;
import com.example.service.NotificationService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/examApproval")
public class ExamApprovalController {

    private static final Set<String> ADMIN_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name());
    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private GradingSubmissionMapper gradingSubmissionMapper;

    @Resource
    private AdminService adminService;

    @Resource
    private ExaminerService examinerService;

    /** 获取待审批记录（examStatus = UNDER_REVIEW） */
    @GetMapping("/getPending")
    public Result getPending(@RequestParam(required = false) Integer examId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        ExamRecord query = new ExamRecord();
        query.setExamStatus("UNDER_REVIEW");
        query.setExamId(examId);
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> list = examRecordService.selectAll(query);
        return Result.success(PageInfo.of(list));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Map<String, Object> data = new HashMap<>();
        data.put("record", examRecordService.getDetail(id));
        data.put("submissions", gradingSubmissionMapper.selectByRecordId(id));
        return Result.success(data);
    }

    @PutMapping("/chiefExaminer/{id}")
    public Result assignChiefExaminer(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        requireRole(ADMIN_ROLES);
        Integer chiefId = ((Number) params.get("chiefExaminerId")).intValue();
        String chiefRole = String.valueOf(params.getOrDefault("chiefExaminerRole", RoleEnum.HELPER.name()));
        String chiefName = resolveName(chiefId, chiefRole);
        ExamRecord record = new ExamRecord();
        record.setId(id);
        record.setChiefExaminerId(chiefId);
        record.setChiefExaminerRole(chiefRole);
        record.setChiefExaminerName(chiefName);
        examRecordService.updateById(record);
        return Result.success();
    }

    /** 主考官最终判定通过 */
    @PutMapping("/approve/{id}")
    public Result approve(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        Account current = requireRole(REVIEW_ROLES);
        ExamRecord detail = examRecordService.getDetail(id);
        if (detail == null) return Result.error("记录不存在");
        if (!canMakeFinalDecision(current, detail)) return Result.error("只有主考官可以最终判定，管理员可先更换主考官");

        ExamRecord record = new ExamRecord();
        record.setId(id);
        record.setExamStatus("PASSED");
        record.setApprovedBy(current.getId());
        record.setApprovedAt(LocalDateTime.now());
        record.setFinalDecisionReason((String) params.get("reason"));
        record.setFinalEvaluatedAt(LocalDateTime.now());
        examRecordService.updateById(record);

        String examName = detail.getExamName() != null ? detail.getExamName() : "审核考核";
        notificationService.notifyApprovalPassed(detail.getStudentId(), examName);
        return Result.success();
    }

    /** 批量审批通过 */
    @PutMapping("/batchApprove")
    public Result batchApprove(@RequestBody Map<String, Object> params) {
        Account current = requireRole(REVIEW_ROLES);
        @SuppressWarnings("unchecked")
        List<Number> idNumbers = (List<Number>) params.get("ids");
        if (idNumbers == null || idNumbers.isEmpty()) return Result.error("请选择审批记录");

        for (Number number : idNumbers) {
            ExamRecord detail = examRecordService.getDetail(number.intValue());
            if (detail == null) return Result.error("记录不存在");
            if (!canMakeFinalDecision(current, detail)) return Result.error("只有主考官可以最终判定，管理员可先更换主考官");
        }
        for (Number number : idNumbers) {
            approve(number.intValue(), params);
        }
        return Result.success();
    }

    /** 主考官最终判定不通过 */
    @PutMapping("/reject/{id}")
    public Result reject(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        Account current = requireRole(REVIEW_ROLES);
        ExamRecord detail = examRecordService.getDetail(id);
        if (detail == null) return Result.error("记录不存在");
        if (!canMakeFinalDecision(current, detail)) return Result.error("只有主考官可以最终判定，管理员可先更换主考官");

        ExamRecord record = new ExamRecord();
        record.setId(id);
        record.setExamStatus("FAILED");
        record.setApprovedBy(current.getId());
        record.setApprovedAt(LocalDateTime.now());
        record.setFinalDecisionReason((String) params.get("reason"));
        record.setFinalEvaluatedAt(LocalDateTime.now());
        examRecordService.updateById(record);

        String examName = detail.getExamName() != null ? detail.getExamName() : "审核考核";
        notificationService.notifyApprovalFailed(detail.getStudentId(), examName);
        return Result.success();
    }

    /** 公示考试结果 */
    @PutMapping("/publishResults/{examId}")
    public Result publishResults(@PathVariable Integer examId) {
        examRecordService.publishByExamId(examId);
        return Result.success();
    }

    /** 取消公示 */
    @PutMapping("/unpublishResults/{examId}")
    public Result unpublishResults(@PathVariable Integer examId) {
        examRecordService.unpublishByExamId(examId);
        return Result.success();
    }

    /** 获取阅卷进度 */
    @GetMapping("/getGradingProgress/{examId}")
    public Result getGradingProgress(@PathVariable Integer examId) {
        Map<String, Object> progress = examRecordService.getGradingProgress(examId);
        return Result.success(progress);
    }

    /** 获取已通过的审批列表 */
    @GetMapping("/approved")
    public Result getApproved(@RequestParam(required = false) Integer examId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        ExamRecord query = new ExamRecord();
        query.setExamStatus("PASSED");
        query.setExamId(examId);
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> list = examRecordService.selectAll(query);
        return Result.success(PageInfo.of(list));
    }

    /** 获取未通过的审批列表 */
    @GetMapping("/rejected")
    public Result getRejected(@RequestParam(required = false) Integer examId,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        ExamRecord query = new ExamRecord();
        query.setExamStatus("FAILED");
        query.setExamId(examId);
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> list = examRecordService.selectAll(query);
        return Result.success(PageInfo.of(list));
    }

    private Account requireRole(Set<String> roles) {
        Account account = TokenUtils.getCurrentUser();
        if (account == null || !roles.contains(account.getRole())) {
            throw new CustomException("403", "无权限访问");
        }
        return account;
    }

    private boolean canMakeFinalDecision(Account current, ExamRecord record) {
        return current.getId().equals(record.getChiefExaminerId()) && current.getRole().equals(record.getChiefExaminerRole());
    }

    private String resolveName(Integer id, String role) {
        if (RoleEnum.OWNER.name().equals(role) || RoleEnum.ADMIN.name().equals(role)) {
            Admin admin = adminService.selectById(id);
            return admin != null ? admin.getName() : String.valueOf(id);
        }
        Examiner examiner = examinerService.selectById(id);
        return examiner != null ? examiner.getName() : String.valueOf(id);
    }
}
