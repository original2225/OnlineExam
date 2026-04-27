package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Question;
import com.example.exception.CustomException;
import com.example.mapper.QuestionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 题目业务层
 */
@Service
public class QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    public void add(Question question) {
        if (ObjectUtil.isEmpty(question.getType())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        if (ObjectUtil.isEmpty(question.getStatus())) {
            question.setStatus("active");
        }
        // 设置默认分值
        if (ObjectUtil.isNull(question.getScore())) {
            String type = question.getType();
            switch (type) {
                case "single":
                case "judge":
                    question.setScore(new BigDecimal("2.0"));
                    break;
                case "multiple":
                    question.setScore(new BigDecimal("5.0"));
                    break;
                case "essay":
                    question.setScore(new BigDecimal("10.0"));
                    break;
                default:
                    question.setScore(new BigDecimal("2.0"));
            }
        }
        if (ObjectUtil.isEmpty(question.getDifficulty())) {
            question.setDifficulty("medium");
        }
        questionMapper.insert(question);
    }

    public void updateById(Question question) {
        questionMapper.updateById(question);
    }

    public void deleteById(Integer id) {
        questionMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            questionMapper.deleteById(id);
        }
    }

    public Question selectById(Integer id) {
        return questionMapper.selectById(id);
    }

    public List<Question> selectAll(Question question) {
        return questionMapper.selectAll(question);
    }

    public PageInfo<Question> selectPage(Question question, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> list = questionMapper.selectAll(question);
        return PageInfo.of(list);
    }

    /**
     * 根据试卷ID获取题目列表
     */
    public List<Question> selectByPaperId(Integer paperId) {
        return questionMapper.selectByPaperId(paperId);
    }

    /**
     * 批量添加题目到试卷
     */
    public void batchAddToPaper(Integer paperId, List<Integer> questionIds) {
        // 这个方法会在试卷服务中实现
    }
}
