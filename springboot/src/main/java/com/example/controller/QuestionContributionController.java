package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.Examiner;
import com.example.entity.Question;
import com.example.entity.QuestionContribution;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.QuestionContributionMapper;
import com.example.mapper.QuestionMapper;
import com.example.utils.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/questionContribution")
public class QuestionContributionController {

    private static final Set<String> SUBMIT_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());
    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private QuestionContributionMapper questionContributionMapper;

    @Resource
    private QuestionMapper questionMapper;

    @PostMapping("/submit")
    public Result submit(@RequestBody QuestionContribution qc) {
        Account current = requireCurrentUser(SUBMIT_ROLES);
        qc.setUserId(current.getId());
        qc.setUserName(getName(current));
        qc.setUserRole(current.getRole());
        qc.setType(normalizeType(qc.getType()));
        qc.setStatus("PENDING");
        questionContributionMapper.insert(qc);
        return Result.success();
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        requireCurrentUser(REVIEW_ROLES);
        PageHelper.startPage(pageNum, pageSize);
        return Result.success(PageInfo.of(questionContributionMapper.selectAll(status)));
    }

    @GetMapping("/myContributions")
    public Result myContributions() {
        Account current = requireCurrentUser(SUBMIT_ROLES);
        return Result.success(questionContributionMapper.selectByUser(current.getId(), current.getRole()));
    }

    @GetMapping("/pendingCount")
    public Result pendingCount() {
        requireCurrentUser(REVIEW_ROLES);
        return Result.success(questionContributionMapper.selectAll("PENDING").size());
    }

    @PutMapping("/review/{id}")
    @Transactional
    public Result review(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        Account reviewer = requireCurrentUser(REVIEW_ROLES);
        QuestionContribution qc = questionContributionMapper.selectById(id);
        if (qc == null) return Result.error("记录不存在");

        String action = String.valueOf(params.getOrDefault("action", ""));
        String comment = (String) params.get("comment");

        qc.setReviewerId(reviewer.getId());
        qc.setReviewerName(getName(reviewer));
        qc.setReviewComment(comment);
        qc.setReviewedAt(LocalDateTime.now());

        if ("approve".equals(action)) {
            qc.setStatus("APPROVED");
            Question question = new Question();
            question.setCategoryId(qc.getCategoryId());
            question.setType(normalizeType(qc.getType()));
            question.setContent(qc.getContent());
            question.setOptions(parseOptions(qc.getOptions()));
            question.setAnswer(qc.getAnswer());
            question.setAnalysis(qc.getAnalysis());
            question.setDifficulty(qc.getDifficulty());
            question.setScore(BigDecimal.TEN);
            question.setCreatedBy(qc.getUserId());
            question.setStatus("active");
            questionMapper.insert(question);
            qc.setApprovedQuestionId(question.getId());
        } else if ("reject".equals(action)) {
            qc.setStatus("REJECTED");
        } else {
            return Result.error("审核操作无效");
        }

        questionContributionMapper.updateById(qc);
        return Result.success();
    }

    private Account requireCurrentUser(Set<String> allowedRoles) {
        Account account = TokenUtils.getCurrentUser();
        if (account == null || !allowedRoles.contains(account.getRole())) {
            throw new CustomException("403", "无权限访问");
        }
        return account;
    }

    private String getName(Account account) {
        if (account instanceof Admin admin) return admin.getName();
        if (account instanceof Examiner examiner) return examiner.getName();
        if (account instanceof Student student) return student.getName();
        return account.getUsername();
    }

    private String normalizeType(String type) {
        return switch (type == null ? "" : type) {
            case "选择题", "单选题", "single" -> "single";
            case "多选题", "multiple" -> "multiple";
            case "判断题", "judge" -> "judge";
            case "填空题", "fill", "fillin" -> "fillin";
            case "简答题", "essay" -> "essay";
            default -> type;
        };
    }

    private Map<String, String> parseOptions(String options) {
        if (options == null || options.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(options, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
