package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.ExamPaper;
import com.example.entity.ExamPaperQuestion;
import com.example.entity.Question;
import com.example.exception.CustomException;
import com.example.mapper.ExamPaperMapper;
import com.example.mapper.ExamPaperQuestionMapper;
import com.example.mapper.QuestionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 试卷业务层
 */
@Service
public class ExamPaperService {

    @Resource
    private ExamPaperMapper examPaperMapper;

    @Resource
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Resource
    private QuestionMapper questionMapper;

    public void add(ExamPaper paper) {
        if (ObjectUtil.isEmpty(paper.getName())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        if (ObjectUtil.isEmpty(paper.getStatus())) {
            paper.setStatus("active");
        }
        if (ObjectUtil.isNull(paper.getTotalScore())) {
            paper.setTotalScore(new BigDecimal("100"));
        }
        if (ObjectUtil.isNull(paper.getPassScore())) {
            paper.setPassScore(new BigDecimal("60"));
        }
        if (ObjectUtil.isEmpty(paper.getType())) {
            paper.setType("manual");
        }
        examPaperMapper.insert(paper);
    }

    @Transactional
    public void updateById(ExamPaper paper) {
        examPaperMapper.updateById(paper);
        // 如果有题目列表，更新试卷题目
        if (paper.getQuestions() != null && !paper.getQuestions().isEmpty()) {
            updatePaperQuestions(paper.getId(), paper.getQuestions());
        }
    }

    public void deleteById(Integer id) {
        // 删除试卷题目关联
        examPaperQuestionMapper.deleteByPaperId(id);
        // 删除试卷
        examPaperMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    public ExamPaper selectById(Integer id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper != null) {
            // 加载试卷题目
            List<Question> questions = questionMapper.selectByPaperId(id);
            paper.setQuestions(questions);
        }
        return paper;
    }

    public List<ExamPaper> selectAll(ExamPaper paper) {
        return examPaperMapper.selectAll(paper);
    }

    public PageInfo<ExamPaper> selectPage(ExamPaper paper, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExamPaper> list = examPaperMapper.selectAll(paper);
        return PageInfo.of(list);
    }

    /**
     * 添加题目到试卷
     */
    @Transactional
    public void addQuestionsToPaper(Integer paperId, List<Integer> questionIds, BigDecimal defaultScore) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }

        // 获取当前试卷题目数量
        List<Question> existingQuestions = questionMapper.selectByPaperId(paperId);
        int currentOrder = existingQuestions.size() + 1;

        List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
        for (Integer questionId : questionIds) {
            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                ExamPaperQuestion pq = new ExamPaperQuestion();
                pq.setPaperId(paperId);
                pq.setQuestionId(questionId);
                pq.setQuestionOrder(currentOrder++);
                pq.setScore(defaultScore != null ? defaultScore : question.getScore());
                paperQuestions.add(pq);
            }
        }

        if (!paperQuestions.isEmpty()) {
            examPaperMapper.batchInsertQuestions(paperQuestions);
        }
    }

    /**
     * 更新试卷题目
     */
    @Transactional
    public void updatePaperQuestions(Integer paperId, List<Question> questions) {
        // 删除原有题目
        examPaperMapper.deleteQuestions(paperId);

        // 重新添加题目
        List<ExamPaperQuestion> paperQuestions = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            ExamPaperQuestion pq = new ExamPaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(q.getId());
            pq.setQuestionOrder(i + 1);
            pq.setScore(q.getScore() != null ? q.getScore() : new BigDecimal("2.0"));
            paperQuestions.add(pq);
        }

        if (!paperQuestions.isEmpty()) {
            examPaperMapper.batchInsertQuestions(paperQuestions);
        }
    }

    /**
     * 删除试卷题目
     */
    @Transactional
    public void removeQuestionFromPaper(Integer paperId, Integer questionId) {
        examPaperQuestionMapper.deleteByPaperId(paperId);
    }
}
