package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamAnnouncement;
import com.example.service.ExamAnnouncementService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examAnnouncement")
public class ExamAnnouncementController {

    @Resource
    private ExamAnnouncementService examAnnouncementService;

    @PostMapping("/add")
    public Result add(@RequestBody ExamAnnouncement announcement) {
        examAnnouncementService.add(announcement);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody ExamAnnouncement announcement) {
        examAnnouncementService.updateById(announcement);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examAnnouncementService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll(ExamAnnouncement announcement) {
        List<ExamAnnouncement> list = examAnnouncementService.selectAll(announcement);
        return Result.success(list);
    }

    @GetMapping("/selectPublished")
    public Result selectPublished() {
        List<ExamAnnouncement> list = examAnnouncementService.selectPublished();
        return Result.success(list);
    }

    @GetMapping("/selectByExam/{examId}")
    public Result selectByExam(@PathVariable Integer examId) {
        List<ExamAnnouncement> list = examAnnouncementService.selectByExamId(examId);
        return Result.success(list);
    }
}
