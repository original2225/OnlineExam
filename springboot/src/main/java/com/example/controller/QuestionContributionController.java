package com.example.controller;

import com.example.common.Result;
import com.example.entity.Question;
import com.example.entity.QuestionContribution;
import com.example.mapper.QuestionContributionMapper;
import com.example.mapper.QuestionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questionContribution")
public class QuestionContributionController {

    @Resource
    private QuestionContributionMapper questionContributionMapper;

    @Resource
    private QuestionMapper questionMapper;

    @PostMapping("/submit")
    public Result submit(@RequestBody QuestionContribution qc) {
        qc.setStatus("PENDING");
        questionContributionMapper.insert(qc);
        return Result.success();
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<QuestionContribution> list = questionContributionMapper.selectAll(status);
        return Result.success(PageInfo.of(list));
    }

    @GetMapping("/myContributions")
    public Result myContributions(@RequestParam Integer userId, @RequestParam String userRole) {
        List<QuestionContribution> list = questionContributionMapper.selectByUser(userId, userRole);
        return Result.success(list);
    }

    /**
     * 获取待审核题目数量
     */
    @GetMapping("/pendingCount")
    public Result pendingCount() {
        List<QuestionContribution> list = questionContributionMapper.selectAll("PENDING");
        return Result.success(list.size());
    }

    @PutMapping("/review/{id}")
    @Transactional
    public Result review(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        QuestionContribution qc = questionContributionMapper.selectById(id);
        if (qc == null) return Result.error("记录不存在");

        String action = (String) params.get("action"); // approve / reject
        Integer reviewerId = params.get("reviewerId") != null ? ((Number) params.get("reviewerId")).intValue() : null;
        String reviewerName = (String) params.get("reviewerName");
        String comment = (String) params.get("comment");

        qc.setReviewerId(reviewerId);
        qc.setReviewerName(reviewerName);
        qc.setReviewComment(comment);
        qc.setReviewedAt(LocalDateTime.now());

        if ("approve".equals(action)) {
            qc.setStatus("APPROVED");
            questionContributionMapper.updateById(qc);
            // 自动创建题目
            Question question = new Question();
            question.setCategoryId(qc.getCategoryId());
            question.setType(qc.getType());
            question.setContent(qc.getContent());
            question.setAnswer(qc.getAnswer());
            question.setAnalysis(qc.getAnalysis());
            question.setDifficulty(qc.getDifficulty());
            question.setScore(BigDecimal.TEN);
            question.setCreatedBy(qc.getUserId());
            question.setStatus("active");
            questionMapper.insert(question);
        } else {
            qc.setStatus("REJECTED");
            questionContributionMapper.updateById(qc);
        }
        return Result.success();
    }
}
