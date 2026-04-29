package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.WrongQuestion;
import com.example.mapper.WrongQuestionMapper;
import com.example.entity.Question;
import com.example.mapper.QuestionMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题集接口
 */
@RestController
@RequestMapping("/wrongQuestion")
public class WrongQuestionController {

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    @Resource
    private QuestionMapper questionMapper;

    /**
     * 添加错题
     */
    @PostMapping("/add")
    public Result add(@RequestBody WrongQuestion wrongQuestion) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        wrongQuestion.setUserId(current.getId());
        wrongQuestion.setUserRole(current.getRole());
        // 检查是否已存在
        int exists = wrongQuestionMapper.existsByUserAndQuestion(
                wrongQuestion.getUserId(), wrongQuestion.getUserRole(), wrongQuestion.getQuestionId());
        if (exists > 0) {
            return Result.success("已收藏");
        }
        wrongQuestionMapper.insert(wrongQuestion);
        return Result.success();
    }

    /**
     * 获取用户的错题列表
     */
    @GetMapping("/selectByUser")
    public Result selectByUser(@RequestParam Integer userId, @RequestParam String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !current.getId().equals(userId) || !current.getRole().equals(userRole)) {
            return forbidden();
        }
        List<WrongQuestion> list = wrongQuestionMapper.selectByUser(userId, userRole);
        // 填充题目信息
        for (WrongQuestion wq : list) {
            if (wq.getQuestion() == null) {
                Question q = questionMapper.selectById(wq.getQuestionId());
                wq.setQuestion(q);
            }
        }
        return Result.success(list);
    }

    /**
     * 删除错题记录
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        wrongQuestionMapper.deleteByOwner(id, current.getId(), current.getRole());
        return Result.success();
    }

    private Result forbidden() {
        return Result.error("403", "无权限访问");
    }
}
