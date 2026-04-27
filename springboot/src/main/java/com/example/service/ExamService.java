package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Exam;
import com.example.entity.ExamPaper;
import com.example.entity.ExamPermission;
import com.example.exception.CustomException;
import com.example.mapper.ExamMapper;
import com.example.mapper.ExamPermissionMapper;
import com.example.mapper.ExamPaperMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试业务层
 */
@Service
public class ExamService {

    @Resource
    private ExamMapper examMapper;

    @Resource
    private ExamPermissionMapper examPermissionMapper;

    @Resource
    private ExamPaperMapper examPaperMapper;

    public void add(Exam exam) {
        if (ObjectUtil.isEmpty(exam.getName()) || ObjectUtil.isNull(exam.getPaperId())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        if (ObjectUtil.isEmpty(exam.getStatus())) {
            exam.setStatus("draft");
        }
        if (ObjectUtil.isNull(exam.getAllowLate())) {
            exam.setAllowLate(false);
        }
        examMapper.insert(exam);
    }

    @Transactional
    public void updateById(Exam exam) {
        examMapper.updateById(exam);
    }

    @Transactional
    public void deleteById(Integer id) {
        // 删除考试权限
        examPermissionMapper.deleteByExamId(id);
        // 删除考试
        examMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    public Exam selectById(Integer id) {
        return examMapper.selectById(id);
    }

    public List<Exam> selectAll(Exam exam) {
        return examMapper.selectAll(exam);
    }

    public PageInfo<Exam> selectPage(Exam exam, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Exam> list = examMapper.selectAll(exam);
        return PageInfo.of(list);
    }

    /**
     * 发布考试
     */
    public void publish(Integer id) {
        examMapper.updateStatus(id, "published");
    }

    /**
     * 取消考试
     */
    public void cancel(Integer id) {
        examMapper.updateStatus(id, "cancelled");
    }

    /**
     * 设置考试权限（允许参加考试的学生）
     */
    @Transactional
    public void setPermissions(Integer examId, List<Integer> studentIds) {
        // 删除原有权限
        examPermissionMapper.deleteByExamId(examId);

        // 批量添加新权限
        if (studentIds != null && !studentIds.isEmpty()) {
            List<ExamPermission> permissions = new ArrayList<>();
            for (Integer studentId : studentIds) {
                ExamPermission permission = new ExamPermission();
                permission.setExamId(examId);
                permission.setStudentId(studentId);
                permissions.add(permission);
            }
            examPermissionMapper.batchInsert(permissions);
        }
    }

    /**
     * 获取考试的权限列表
     */
    public List<ExamPermission> getPermissions(Integer examId) {
        ExamPermission permission = new ExamPermission();
        permission.setExamId(examId);
        return examPermissionMapper.selectAll(permission);
    }

    /**
     * 检查学生是否有权限参加考试
     */
    public boolean checkPermission(Integer examId, Integer studentId) {
        List<ExamPermission> permissions = examPermissionMapper.selectByExamId(examId);
        return permissions.stream().anyMatch(p -> p.getStudentId().equals(studentId));
    }

    /**
     * 获取学生可参加的考试列表
     */
    public List<Exam> getAvailableExams(Integer studentId) {
        List<ExamPermission> permissions = examPermissionMapper.selectByStudentId(studentId);
        List<Exam> availableExams = new ArrayList<>();
        for (ExamPermission permission : permissions) {
            Exam exam = examMapper.selectById(permission.getExamId());
            if (exam != null && ("published".equals(exam.getStatus()) || "ongoing".equals(exam.getStatus()))) {
                availableExams.add(exam);
            }
        }
        return availableExams;
    }

    /**
     * 为指定学生创建补考
     * 复用原试卷，创建新的补考考试实例
     */
    @Transactional
    public Map<String, Object> createMakeupExam(Integer originalExamId, List<Integer> studentIds,
                                                  String startTime, String endTime, Integer duration) {
        Exam original = examMapper.selectById(originalExamId);
        if (original == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime, fmt) : LocalDateTime.now();
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime, fmt) : start.plusMinutes(duration != null ? duration : original.getDuration());

        // 创建补考考试
        Exam makeup = new Exam();
        makeup.setName("[补考] " + original.getName());
        makeup.setPaperId(original.getPaperId());
        makeup.setDescription("补考 — 基于「" + original.getName() + "」试卷");
        makeup.setExamType("scheduled");
        makeup.setStartTime(start);
        makeup.setEndTime(end);
        makeup.setDuration(duration != null ? duration : original.getDuration());
        makeup.setAllowLate(true);
        makeup.setLateTime(15);
        makeup.setStatus("published");
        makeup.setBranch(original.getBranch());
        makeup.setAutoPublish(false);
        makeup.setEnableRecording(false);
        examMapper.insert(makeup);

        // 为指定学生设置权限
        List<ExamPermission> permissions = new ArrayList<>();
        for (Integer studentId : studentIds) {
            ExamPermission p = new ExamPermission();
            p.setExamId(makeup.getId());
            p.setStudentId(studentId);
            permissions.add(p);
        }
        if (!permissions.isEmpty()) {
            examPermissionMapper.batchInsert(permissions);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("examId", makeup.getId());
        result.put("examName", makeup.getName());
        result.put("studentCount", studentIds.size());
        return result;
    }
}
