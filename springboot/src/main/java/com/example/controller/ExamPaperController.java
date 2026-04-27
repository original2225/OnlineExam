package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamPaper;
import com.example.service.ExamPaperService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷前端请求接口
 */
@RestController
@RequestMapping("/examPaper")
public class ExamPaperController {

    @Resource
    private ExamPaperService examPaperService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody ExamPaper paper) {
        examPaperService.add(paper);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody ExamPaper paper) {
        examPaperService.updateById(paper);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examPaperService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        examPaperService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ExamPaper paper = examPaperService.selectById(id);
        return Result.success(paper);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(ExamPaper paper) {
        List<ExamPaper> list = examPaperService.selectAll(paper);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(ExamPaper paper,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ExamPaper> pageInfo = examPaperService.selectPage(paper, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 添加题目到试卷
     */
    @PostMapping("/addQuestions")
    public Result addQuestions(@RequestParam Integer paperId,
                               @RequestBody List<Integer> questionIds,
                               @RequestParam(required = false) BigDecimal defaultScore) {
        examPaperService.addQuestionsToPaper(paperId, questionIds, defaultScore);
        return Result.success();
    }

}
