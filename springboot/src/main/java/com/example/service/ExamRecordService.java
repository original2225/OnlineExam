package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.ExamAnswerMapper;
import com.example.mapper.ExamMapper;
import com.example.mapper.ExamPaperMapper;
import com.example.mapper.ExamRecordMapper;
import com.example.mapper.QuestionMapper;
import com.example.mapper.StudentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试记录业务层
 */
@Service
public class ExamRecordService {

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    @Resource
    private ExamMapper examMapper;

    @Resource
    private ExamPaperMapper examPaperMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private NotificationService notificationService;

    /**
     * 开始考试
     */
    @Transactional
    public ExamRecord startExam(Integer examId, Integer studentId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        ExamRecord existingRecord = examRecordMapper.selectOngoingRecord(examId, studentId);
        if (existingRecord != null) {
            return existingRecord;
        }
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setPaperId(exam.getPaperId());
        record.setStudentId(studentId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("ongoing");
        record.setAutoScore(BigDecimal.ZERO);
        record.setManualScore(BigDecimal.ZERO);
        record.setTotalScore(BigDecimal.ZERO);
        examRecordMapper.insert(record);
        return record;
    }

    /**
     * 保存答案（自动保存）
     */
    @Transactional
    public void saveAnswer(Integer recordId, Integer questionId, String studentAnswer) {
        ExamAnswer answer = examAnswerMapper.selectByRecordAndQuestion(recordId, questionId);
        if (answer == null) {
            answer = new ExamAnswer();
            answer.setRecordId(recordId);
            answer.setQuestionId(questionId);
            answer.setStudentAnswer(studentAnswer);
            answer.setScore(BigDecimal.ZERO);
            examAnswerMapper.insert(answer);
        } else {
            answer.setStudentAnswer(studentAnswer);
            examAnswerMapper.updateById(answer);
        }
    }

    /**
     * 批量保存答案
     */
    @Transactional
    public void batchSaveAnswers(Integer recordId, List<ExamAnswer> answers) {
        for (ExamAnswer answer : answers) {
            answer.setRecordId(recordId);
            ExamAnswer existing = examAnswerMapper.selectByRecordAndQuestion(recordId, answer.getQuestionId());
            if (existing != null) {
                existing.setStudentAnswer(answer.getStudentAnswer());
                examAnswerMapper.updateById(existing);
            } else {
                answer.setScore(BigDecimal.ZERO);
                examAnswerMapper.insert(answer);
            }
        }
    }

    /**
     * 提交考试
     */
    @Transactional
    public ExamRecord submitExam(Integer recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !"ongoing".equals(record.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        LocalDateTime submitTime = LocalDateTime.now();
        record.setSubmitTime(submitTime);
        Duration duration = Duration.between(record.getStartTime(), submitTime);
        record.setDuration((int) duration.getSeconds());
        autoGrade(record);
        record.setStatus("completed");
        record.setExamStatus("PENDING");
        examRecordMapper.updateById(record);
        assignBranchOnPass(record);

        // 通知管理员有新审核提交
        Exam exam = examMapper.selectById(record.getExamId());
        String examName = exam != null ? exam.getName() : "审核";
        Student student = studentMapper.selectById(record.getStudentId());
        String studentName = student != null ? student.getName() : "玩家";
        notificationService.notifyExamSubmitted(studentName, examName);

        return record;
    }

    /**
     * 自动判分
     */
    private void autoGrade(ExamRecord record) {
        List<Question> questions = questionMapper.selectByPaperId(record.getPaperId());
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal autoScore = BigDecimal.ZERO;

        for (Question question : questions) {
            ExamAnswer answer = examAnswerMapper.selectByRecordAndQuestion(record.getId(), question.getId());
            if (answer == null) {
                continue;
            }

            boolean isCorrect = false;
            BigDecimal questionScore = BigDecimal.ZERO;

            if ("single".equals(question.getType()) || "judge".equals(question.getType())) {
                // 单选题和判断题：完全匹配（加判空防止NPE）
                if (question.getAnswer() != null && answer.getStudentAnswer() != null) {
                    isCorrect = question.getAnswer().equalsIgnoreCase(answer.getStudentAnswer());
                }
                if (isCorrect) {
                    questionScore = question.getScore();
                }
            } else if ("multiple".equals(question.getType())) {
                // 多选题：排序后比较
                String studentAns = answer.getStudentAnswer();
                String correctAns = question.getAnswer();
                String sortedStudent = sortString(studentAns);
                String sortedCorrect = sortString(correctAns);
                isCorrect = sortedStudent.equalsIgnoreCase(sortedCorrect);
                if (isCorrect) {
                    questionScore = question.getScore();
                }
            } else {
                // 简答题/填空题：需人工判分，暂给0分
                isCorrect = false;
                questionScore = BigDecimal.ZERO;
            }

            answer.setIsCorrect(isCorrect);
            answer.setScore(questionScore);
            examAnswerMapper.updateById(answer);
            totalScore = totalScore.add(question.getScore());
            autoScore = autoScore.add(questionScore);
        }

        record.setAutoScore(autoScore);
        record.setTotalScore(autoScore);

        // 检查是否及格
        Exam exam = examMapper.selectById(record.getExamId());
        if (exam != null) {
            ExamPaper paper = examPaperMapper.selectById(exam.getPaperId());
            if (paper != null && record.getTotalScore() != null) {
                boolean isPass = record.getTotalScore().compareTo(paper.getPassScore()) >= 0;
                record.setIsPass(isPass);
            }
        }
    }

    private String sortString(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        java.util.Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * 获取考试记录详情
     */
    public ExamRecord getDetail(Integer recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record != null) {
            List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(recordId);
            for (ExamAnswer answer : answers) {
                Question question = questionMapper.selectById(answer.getQuestionId());
                answer.setQuestion(question);
            }
            record.setAnswers(answers);
        }
        return record;
    }

    public List<ExamRecord> selectAll(ExamRecord record) {
        return examRecordMapper.selectAll(record);
    }

    public PageInfo<ExamRecord> selectPage(ExamRecord record, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ExamRecord> list = examRecordMapper.selectAll(record);
        return PageInfo.of(list);
    }

    public List<ExamRecord> getByStudentId(Integer studentId) {
        return examRecordMapper.selectByStudentId(studentId);
    }

    public List<ExamRecord> getByExamId(Integer examId) {
        return examRecordMapper.selectByExamId(examId);
    }

    public List<ExamRecord> getPublicResults() {
        return examRecordMapper.selectPublicResults();
    }

    public void updateById(ExamRecord record) {
        examRecordMapper.updateById(record);
    }

    public Map<String, Object> getGradingProgress(Integer examId) {
        return examRecordMapper.getGradingProgress(examId);
    }

    public void publishByExamId(Integer examId) {
        examRecordMapper.publishByExamId(examId, LocalDateTime.now());
    }

    public void unpublishByExamId(Integer examId) {
        examRecordMapper.unpublishByExamId(examId);
    }

    /**
     * 考试通过后自动分配分支头衔
     */
    private void assignBranchOnPass(ExamRecord record) {
        if (record.getIsPass() == null || !record.getIsPass()) {
            return;
        }
        Exam exam = examMapper.selectById(record.getExamId());
        if (exam == null || exam.getBranch() == null || exam.getBranch().isEmpty()) {
            return;
        }
        Student student = studentMapper.selectById(record.getStudentId());
        if (student == null) {
            return;
        }
        String currentBranch = student.getBranch();
        if (currentBranch != null && !currentBranch.isEmpty() && !currentBranch.endsWith("见习")) {
            return;
        }
        String newBranch = exam.getBranch() + "见习";
        student.setBranch(newBranch);
        studentMapper.updateById(student);
    }
}
