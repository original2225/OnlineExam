package com.example.controller;

import com.example.common.Result;
import com.example.entity.Examiner;
import com.example.service.ExaminerService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/examiner")
public class ExaminerController {

    @Resource
    private ExaminerService examinerService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Examiner examiner) {
        examinerService.add(examiner);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Examiner examiner) {
        examinerService.updateById(examiner);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        examinerService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        examinerService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Examiner examiner = examinerService.selectById(id);
        return Result.success(examiner);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Examiner examiner) {
        List<Examiner> list = examinerService.selectAll(examiner);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Examiner examiner,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Examiner> pageInfo = examinerService.selectPage(examiner, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
