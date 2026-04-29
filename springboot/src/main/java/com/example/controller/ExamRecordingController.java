package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.ExamRecord;
import com.example.entity.ExamRecording;
import com.example.service.CloudreveService;
import com.example.service.ExamRecordService;
import com.example.service.ExamRecordingService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/examRecording")
public class ExamRecordingController {

    private static final long MAX_RECORDING_SIZE = 300L * 1024 * 1024;
    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());

    @Resource
    private ExamRecordingService examRecordingService;

    @Resource
    private CloudreveService cloudreveService;

    @Resource
    private ExamRecordService examRecordService;

    /** 创建录屏记录 */
    @PostMapping("/start")
    public Result start(@RequestBody ExamRecording recording) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        if (RoleEnum.USER.name().equals(current.getRole())) {
            if (recording.getRecordId() != null && !canAccessRecord(current, recording.getRecordId())) {
                return forbidden();
            }
            recording.setStudentId(current.getId());
        } else {
            return forbidden();
        }
        examRecordingService.add(recording);
        return Result.success(recording);
    }

    /** 更新录屏记录（上传完成后调用） */
    @PutMapping("/update")
    public Result update(@RequestBody ExamRecording recording) {
        if (!isCurrentStudentRecording(recording.getId())) {
            return forbidden();
        }
        examRecordingService.updateById(recording);
        return Result.success();
    }

    /** 查询某考试的录屏列表 */
    @GetMapping("/selectByExam/{examId}")
    public Result selectByExam(@PathVariable Integer examId) {
        if (!hasReviewPermission()) {
            return forbidden();
        }
        List<ExamRecording> list = examRecordingService.selectByExamId(examId);
        return Result.success(list);
    }

    /** 查询某玩家在某考试的录屏 */
    @GetMapping("/selectByExamAndStudent")
    public Result selectByExamAndStudent(@RequestParam Integer examId, @RequestParam Integer studentId) {
        if (!canAccessStudent(studentId)) {
            return forbidden();
        }
        ExamRecording recording = examRecordingService.selectByExamAndStudent(examId, studentId);
        return Result.success(recording);
    }

    /** 查询所有录屏记录 */
    @GetMapping("/selectAll")
    public Result selectAll(ExamRecording recording) {
        if (!hasReviewPermission()) {
            return forbidden();
        }
        List<ExamRecording> list = examRecordingService.selectAll(recording);
        return Result.success(list);
    }

    /** 上传录屏文件到 Cloudreve */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                          @RequestParam("recordingId") Integer recordingId,
                          @RequestParam("examId") Integer examId,
                          @RequestParam("studentId") Integer studentId) {
        if (!isCurrentStudent(studentId) || !isCurrentStudentRecording(recordingId)) {
            return forbidden();
        }
        ExamRecording existing = examRecordingService.selectById(recordingId);
        if (existing == null || !examId.equals(existing.getExamId()) || !studentId.equals(existing.getStudentId())) {
            return forbidden();
        }
        if (file == null || file.isEmpty()) {
            return Result.error("录屏文件不能为空");
        }
        if (file.getSize() > MAX_RECORDING_SIZE) {
            return Result.error("录屏文件不能超过300MB");
        }
        String contentType = file.getContentType();
        if (!Objects.equals(contentType, "video/webm") && !Objects.equals(contentType, "application/octet-stream")) {
            return Result.error("仅支持webm录屏文件");
        }

        String fileName = "recording_exam_" + examId + "_student_" + studentId + "_" + System.currentTimeMillis() + ".webm";
        String shareUrl = cloudreveService.uploadAndShare(file, fileName);

        ExamRecording recording = new ExamRecording();
        recording.setId(recordingId);
        recording.setFileUrl(shareUrl);
        recording.setFileSize(file.getSize());
        recording.setStatus(shareUrl != null ? "uploaded" : "failed");
        examRecordingService.updateById(recording);

        return Result.success(shareUrl);
    }

    private boolean isCurrentStudentRecording(Integer recordingId) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !RoleEnum.USER.name().equals(current.getRole())) {
            return false;
        }
        ExamRecording recording = examRecordingService.selectById(recordingId);
        return recording != null && current.getId().equals(recording.getStudentId());
    }

    private boolean canAccessRecord(Account current, Integer recordId) {
        if (REVIEW_ROLES.contains(current.getRole())) {
            return true;
        }
        ExamRecord record = examRecordService.getDetail(recordId);
        return RoleEnum.USER.name().equals(current.getRole())
                && record != null
                && current.getId().equals(record.getStudentId());
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
