package com.example.service;

import com.example.entity.ExamAnswer;
import com.example.mapper.ExamAnswerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题业务层
 */
@Service
public class ExamAnswerService {

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    public void updateById(ExamAnswer answer) {
        examAnswerMapper.updateById(answer);
    }

    public ExamAnswer selectById(Integer id) {
        return examAnswerMapper.selectById(id);
    }

    /**
     * 批量更新答案（用于阅卷）
     */
    public void batchUpdateForGrading(List<ExamAnswer> answers, Integer gradedBy) {
        for (ExamAnswer answer : answers) {
            if (answer.getQuestion() != null && "essay".equals(answer.getQuestion().getType())) {
                // 只更新简答题的分数和评语
                answer.setGradedBy(gradedBy);
                answer.setGradedAt(LocalDateTime.now());
                examAnswerMapper.updateById(answer);
            }
        }
    }

    /**
     * 计算主观题总分
     */
    public BigDecimal calculateManualScore(Integer recordId) {
        List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
        BigDecimal manualScore = BigDecimal.ZERO;
        for (ExamAnswer answer : answers) {
            if (answer.getScore() != null) {
                manualScore = manualScore.add(answer.getScore());
            }
        }
        return manualScore;
    }
}
