package com.example.controller;

import com.example.common.Result;
import com.example.entity.QuestionFavorite;
import com.example.entity.Question;
import com.example.mapper.QuestionFavoriteMapper;
import com.example.mapper.QuestionMapper;
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
        if (questionFavoriteMapper.existsByUserAndQuestion(fav.getUserId(), fav.getUserRole(), fav.getQuestionId()) > 0) {
            return Result.success("已收藏");
        }
        questionFavoriteMapper.insert(fav);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionFavoriteMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/selectByUser")
    public Result selectByUser(@RequestParam Integer userId, @RequestParam String userRole) {
        List<QuestionFavorite> list = questionFavoriteMapper.selectByUser(userId, userRole);
        for (QuestionFavorite fav : list) {
            if (fav.getQuestion() == null) {
                fav.setQuestion(questionMapper.selectById(fav.getQuestionId()));
            }
        }
        return Result.success(list);
    }

    @GetMapping("/check")
    public Result check(@RequestParam Integer userId, @RequestParam String userRole, @RequestParam Integer questionId) {
        int count = questionFavoriteMapper.existsByUserAndQuestion(userId, userRole, questionId);
        return Result.success(count > 0);
    }
}
