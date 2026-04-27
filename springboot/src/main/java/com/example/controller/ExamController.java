package com.example.controller;

import com.example.common.Result;
import com.example.entity.Exam;
import com.example.entity.ExamPermission;
import com.example.service.ExamService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考试前端请求接口
 */
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Resource
    private ExamService examService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Exam exam) {
        examService.add(exam);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Exam exam) {
        examService.updateById(exam);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        examService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Exam exam = examService.selectById(id);
        return Result.success(exam);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Exam exam) {
        List<Exam> list = examService.selectAll(exam);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Exam exam,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Exam> pageInfo = examService.selectPage(exam, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 发布考试
     */
    @PutMapping("/publish/{id}")
    public Result publish(@PathVariable Integer id) {
        examService.publish(id);
        return Result.success();
    }

    /**
     * 取消考试
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Integer id) {
        examService.cancel(id);
        return Result.success();
    }

    /**
     * 设置考试权限
     */
    @PostMapping("/setPermissions")
    public Result setPermissions(@RequestParam Integer examId, @RequestBody List<Integer> studentIds) {
        examService.setPermissions(examId, studentIds);
        return Result.success();
    }

    /**
     * 获取考试权限列表
     */
    @GetMapping("/getPermissions/{examId}")
    public Result getPermissions(@PathVariable Integer examId) {
        List<ExamPermission> list = examService.getPermissions(examId);
        return Result.success(list);
    }

    /**
     * 获取学生可参加的考试列表
     */
    @GetMapping("/getAvailableExams/{studentId}")
    public Result getAvailableExams(@PathVariable Integer studentId) {
        List<Exam> list = examService.getAvailableExams(studentId);
        return Result.success(list);
    }

    /**
     * 为指定学生开放补考
     * 管理员/阅卷人可以给未参加考试的学生开放补考
     * 使用原试卷，自定义时间窗口
     */
    @PostMapping("/makeup")
    public Result createMakeupExam(@RequestBody Map<String, Object> params) {
        Integer originalExamId = ((Number) params.get("originalExamId")).intValue();
        List<Integer> studentIds = ((List<Number>) params.get("studentIds")).stream()
                .map(Number::intValue).toList();
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        Integer duration = params.get("duration") != null ? ((Number) params.get("duration")).intValue() : null;

        Map<String, Object> result = examService.createMakeupExam(originalExamId, studentIds, startTime, endTime, duration);
        return Result.success(result);
    }

}
