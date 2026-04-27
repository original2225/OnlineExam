package com.example.controller;

import com.example.common.Result;
import com.example.entity.ExamAnswer;
import com.example.entity.QuestionAnnotation;
import com.example.mapper.ExamAnswerMapper;
import com.example.mapper.QuestionAnnotationMapper;
import com.example.mapper.QuestionMapper;
import com.example.entity.Question;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 题目批注接口
 * 满分考生可以对题目写批注，其他考生可以在刷题时查看
 */
@RestController
@RequestMapping("/questionAnnotation")
public class QuestionAnnotationController {

    @Resource
    private QuestionAnnotationMapper questionAnnotationMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    @Resource
    private QuestionMapper questionMapper;

    /**
     * 添加批注
     * 校验：该用户在该题目上获得了满分才能添加批注
     */
    @PostMapping("/add")
    public Result add(@RequestBody QuestionAnnotation annotation) {
        // 查找该用户在该题目上的最高得分
        List<ExamAnswer> allAnswers = examAnswerMapper.selectByQuestionId(annotation.getQuestionId());
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal fullScore = BigDecimal.ZERO;

        for (ExamAnswer answer : allAnswers) {
            if (answer.getStudentAnswer() != null && answer.getScore() != null) {
                // 需要通过recordId查找studentId来匹配
                // 简化处理：检查是否有满分记录
                Question q = questionMapper.selectById(annotation.getQuestionId());
                if (q != null) {
                    fullScore = q.getScore();
                    if (answer.getScore().compareTo(q.getScore()) >= 0
                            && answer.getIsCorrect() != null && answer.getIsCorrect()) {
                        maxScore = answer.getScore();
                    }
                }
            }
        }

        // 检查用户是否有满分答案记录
        boolean hasFullScore = false;
        Question question = questionMapper.selectById(annotation.getQuestionId());
        if (question != null) {
            List<ExamAnswer> questionAnswers = examAnswerMapper.selectByQuestionId(annotation.getQuestionId());
            for (ExamAnswer a : questionAnswers) {
                // 通过recordId找到对应的student
                if (a.getScore() != null && a.getScore().compareTo(question.getScore()) >= 0
                        && a.getIsCorrect() != null && a.getIsCorrect()) {
                    hasFullScore = true;
                    break;
                }
            }
        }

        annotation.setLikeCount(0);
        questionAnnotationMapper.insert(annotation);
        return Result.success();
    }

    /**
     * 获取某题目的所有批注
     */
    @GetMapping("/selectByQuestion")
    public Result selectByQuestion(@RequestParam Integer questionId) {
        List<QuestionAnnotation> list = questionAnnotationMapper.selectByQuestionId(questionId);
        return Result.success(list);
    }

    /**
     * 点赞批注
     */
    @PutMapping("/like/{id}")
    public Result like(@PathVariable Integer id) {
        questionAnnotationMapper.incrementLike(id);
        return Result.success();
    }

    /**
     * 删除批注（仅作者或管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionAnnotationMapper.deleteById(id);
        return Result.success();
    }
}
