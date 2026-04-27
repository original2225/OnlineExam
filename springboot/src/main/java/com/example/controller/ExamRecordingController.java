package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamRecording;
import com.example.service.CloudreveService;
import com.example.service.ExamRecordingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/examRecording")
public class ExamRecordingController {

    private static final long MAX_RECORDING_SIZE = 300L * 1024 * 1024;

    @Resource
    private ExamRecordingService examRecordingService;

    @Resource
    private CloudreveService cloudreveService;

    /** 创建录屏记录 */
    @PostMapping("/start")
    public Result start(@RequestBody ExamRecording recording) {
        examRecordingService.add(recording);
        return Result.success(recording);
    }

    /** 更新录屏记录（上传完成后调用） */
    @PutMapping("/update")
    public Result update(@RequestBody ExamRecording recording) {
        examRecordingService.updateById(recording);
        return Result.success();
    }

    /** 查询某考试的录屏列表 */
    @GetMapping("/selectByExam/{examId}")
    public Result selectByExam(@PathVariable Integer examId) {
        List<ExamRecording> list = examRecordingService.selectByExamId(examId);
        return Result.success(list);
    }

    /** 查询某玩家在某考试的录屏 */
    @GetMapping("/selectByExamAndStudent")
    public Result selectByExamAndStudent(@RequestParam Integer examId, @RequestParam Integer studentId) {
        ExamRecording recording = examRecordingService.selectByExamAndStudent(examId, studentId);
        return Result.success(recording);
    }

    /** 查询所有录屏记录 */
    @GetMapping("/selectAll")
    public Result selectAll(ExamRecording recording) {
        List<ExamRecording> list = examRecordingService.selectAll(recording);
        return Result.success(list);
    }

    /** 上传录屏文件到 Cloudreve */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("recordingId") Integer recordingId,
                         @RequestParam("examId") Integer examId,
                         @RequestParam("studentId") Integer studentId) {
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
}
