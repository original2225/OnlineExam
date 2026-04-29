package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.ExamAnswer;
import com.example.entity.ExamRecord;
import com.example.entity.Examiner;
import com.example.entity.QuestionAnnotation;
import com.example.entity.Student;
import com.example.mapper.ExamAnswerMapper;
import com.example.mapper.ExamRecordMapper;
import com.example.mapper.QuestionAnnotationMapper;
import com.example.mapper.QuestionMapper;
import com.example.entity.Question;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 题目批注接口
 * 满分玩家可以对题目写批注，其他玩家可以在审核模拟时查看
 */
@RestController
@RequestMapping("/questionAnnotation")
public class QuestionAnnotationController {

    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());

    @Resource
    private QuestionAnnotationMapper questionAnnotationMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private QuestionMapper questionMapper;

    /**
     * 添加批注
     * 校验：该用户在该题目上获得了满分才能添加批注
     */
    @PostMapping("/add")
    public Result add(@RequestBody QuestionAnnotation annotation) {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || !RoleEnum.USER.name().equals(current.getRole())) {
            return forbidden();
        }
        if (annotation.getQuestionId() == null) {
            return Result.error("题目不能为空");
        }
        String content = annotation.getContent() == null ? "" : annotation.getContent().trim();
        if (content.isEmpty()) {
            return Result.error("批注内容不能为空");
        }
        if (content.length() > 1000) {
            return Result.error("批注内容不能超过1000字");
        }

        Question question = questionMapper.selectById(annotation.getQuestionId());
        if (question == null || question.getScore() == null) {
            return Result.error("题目不存在");
        }
        if (!hasFullScoreAnswer(current.getId(), annotation.getQuestionId(), question.getScore())) {
            return forbidden();
        }

        annotation.setUserId(current.getId());
        annotation.setUserName(displayName(current));
        annotation.setUserRole(current.getRole());
        annotation.setContent(content);
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
        if (TokenUtils.getCurrentUser() == null) {
            return forbidden();
        }
        questionAnnotationMapper.incrementLike(id);
        return Result.success();
    }

    /**
     * 删除批注（仅作者或管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account current = TokenUtils.getCurrentUser();
        QuestionAnnotation annotation = questionAnnotationMapper.selectById(id);
        if (current == null || annotation == null) {
            return forbidden();
        }
        boolean isOwner = current.getId().equals(annotation.getUserId()) && current.getRole().equals(annotation.getUserRole());
        if (!isOwner && !REVIEW_ROLES.contains(current.getRole())) {
            return forbidden();
        }
        questionAnnotationMapper.deleteById(id);
        return Result.success();
    }

    private boolean hasFullScoreAnswer(Integer studentId, Integer questionId, BigDecimal fullScore) {
        List<ExamAnswer> questionAnswers = examAnswerMapper.selectByQuestionId(questionId);
        for (ExamAnswer answer : questionAnswers) {
            if (answer.getRecordId() == null || answer.getScore() == null || !Boolean.TRUE.equals(answer.getIsCorrect())) {
                continue;
            }
            if (answer.getScore().compareTo(fullScore) < 0) {
                continue;
            }
            ExamRecord record = examRecordMapper.selectById(answer.getRecordId());
            if (record != null && studentId.equals(record.getStudentId())) {
                return true;
            }
        }
        return false;
    }

    private String displayName(Account account) {
        if (account instanceof Admin admin && admin.getName() != null && !admin.getName().isBlank()) {
            return admin.getName();
        }
        if (account instanceof Examiner examiner && examiner.getName() != null && !examiner.getName().isBlank()) {
            return examiner.getName();
        }
        if (account instanceof Student student && student.getName() != null && !student.getName().isBlank()) {
            return student.getName();
        }
        return account.getUsername();
    }

    private Result forbidden() {
        return Result.error("403", "无权限访问");
    }
}
