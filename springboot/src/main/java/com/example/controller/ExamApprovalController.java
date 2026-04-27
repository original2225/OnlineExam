package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamRecord;
import com.example.mapper.ExamMapper;
import com.example.service.ExamRecordService;
import com.example.service.NotificationService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/examApproval")
public class ExamApprovalController {

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private ExamMapper examMapper;

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

    /** 审批通过单条记录 */
    @PutMapping("/approve/{id}")
    public Result approve(@PathVariable Integer id, @RequestBody Map<String, Integer> params) {
        ExamRecord record = new ExamRecord();
        record.setId(id);
        record.setExamStatus("PASSED");
        record.setApprovedBy(params.get("approvedBy"));
        record.setApprovedAt(LocalDateTime.now());
        examRecordService.updateById(record);

        // 通知学生
        ExamRecord detail = examRecordService.getDetail(id);
        if (detail != null) {
            String examName = detail.getExamName() != null ? detail.getExamName() : "考试";
            notificationService.notifyApprovalPassed(detail.getStudentId(), examName);
        }
        return Result.success();
    }

    /** 批量审批通过 */
    @PutMapping("/batchApprove")
    public Result batchApprove(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Number> idNumbers = (List<Number>) params.get("ids");
        List<Integer> ids = idNumbers.stream().map(Number::intValue).toList();
        Integer approvedBy = ((Number) params.get("approvedBy")).intValue();
        for (Integer id : ids) {
            ExamRecord record = new ExamRecord();
            record.setId(id);
            record.setExamStatus("PASSED");
            record.setApprovedBy(approvedBy);
            record.setApprovedAt(LocalDateTime.now());
            examRecordService.updateById(record);
        }
        return Result.success();
    }

    /** 审批不通过 */
    @PutMapping("/reject/{id}")
    public Result reject(@PathVariable Integer id, @RequestBody Map<String, Integer> params) {
        ExamRecord record = new ExamRecord();
        record.setId(id);
        record.setExamStatus("FAILED");
        record.setApprovedBy(params.get("approvedBy"));
        record.setApprovedAt(LocalDateTime.now());
        examRecordService.updateById(record);

        // 通知学生
        ExamRecord detail = examRecordService.getDetail(id);
        if (detail != null) {
            String examName = detail.getExamName() != null ? detail.getExamName() : "考试";
            notificationService.notifyApprovalFailed(detail.getStudentId(), examName);
        }
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
}
