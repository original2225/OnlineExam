package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.QuestionFavorite;
import com.example.entity.Question;
import com.example.mapper.QuestionFavoriteMapper;
import com.example.mapper.QuestionMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questionFavorite")
public class QuestionFavoriteController {

    @Resource
    private QuestionFavoriteMapper questionFavoriteMapper;

    @Resource
    private QuestionMapper questionMapper;

    @PostMapping("/add")
    public Result add(@RequestBody QuestionFavorite fav) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        fav.setUserId(current.getId());
        fav.setUserRole(current.getRole());
        if (questionFavoriteMapper.existsByUserAndQuestion(fav.getUserId(), fav.getUserRole(), fav.getQuestionId()) > 0) {
            return Result.success("已收藏");
        }
        questionFavoriteMapper.insert(fav);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null) {
            return forbidden();
        }
        questionFavoriteMapper.deleteByOwner(id, current.getId(), current.getRole());
        return Result.success();
    }

    @GetMapping("/selectByUser")
    public Result selectByUser(@RequestParam Integer userId, @RequestParam String userRole) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !current.getId().equals(userId) || !current.getRole().equals(userRole)) {
            return forbidden();
        }
        List<QuestionFavorite> list = questionFavoriteMapper.selectByUser(current.getId(), current.getRole());
        for (QuestionFavorite fav : list) {
            if (fav.getQuestion() == null) {
                fav.setQuestion(questionMapper.selectById(fav.getQuestionId()));
            }
        }
        return Result.success(list);
    }

    @GetMapping("/check")
    public Result check(@RequestParam Integer userId, @RequestParam String userRole, @RequestParam Integer questionId) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !current.getId().equals(userId) || !current.getRole().equals(userRole)) {
            return forbidden();
        }
        int count = questionFavoriteMapper.existsByUserAndQuestion(current.getId(), current.getRole(), questionId);
        return Result.success(count > 0);
    }

    private Result forbidden() {
        return Result.error("403", "无权限访问");
    }
}
