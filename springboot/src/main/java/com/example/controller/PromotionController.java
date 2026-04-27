package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.Examiner;
import com.example.entity.Student;
import com.example.mapper.AdminMapper;
import com.example.mapper.ExaminerMapper;
import com.example.mapper.StudentMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 用户提拔控制器
 * 权限规则：只能提拔到比自己低的权限等级
 * - Owner(level=4): 可提拔到 Admin(3) 或 阅卷人(2)
 * - Admin(level=3): 可提拔到 阅卷人(2)
 * - 阅卷人(2)/用户(1): 无权提拔
 */
@RestController
@RequestMapping("/promotion")
public class PromotionController {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private ExaminerMapper examinerMapper;

    @Resource
    private AdminMapper adminMapper;

    /**
     * 提拔玩家为阅卷人 (→level 2)
     * 需要 Admin(level>=3) 及以上
     */
    @Transactional
    @PostMapping("/studentToExaminer/{studentId}")
    public Result studentToExaminer(@PathVariable Integer studentId) {
        Admin operator = getOperatorAdmin();
        if (operator == null || operator.getLevel() == null || operator.getLevel() <= 2) {
            return Result.error("权限不足，需要 Admin 及以上权限");
        }

        Student student = studentMapper.selectById(studentId);
        if (ObjectUtil.isNull(student)) {
            return Result.error("玩家不存在");
        }

        Examiner existExaminer = examinerMapper.selectByUsername(student.getUsername());
        if (ObjectUtil.isNotNull(existExaminer)) {
            return Result.error("该用户已经是阅卷人");
        }

        Examiner examiner = new Examiner();
        examiner.setUsername(student.getUsername());
        examiner.setPassword(student.getPassword());
        examiner.setName(student.getName());
        examiner.setPhone(student.getPhone());
        examiner.setEmail(student.getEmail());
        examiner.setAvatar(student.getAvatar());
        examiner.setRole("HELPER");
        examinerMapper.insert(examiner);

        student.setStatus("PROMOTED");
        studentMapper.updateById(student);

        return Result.success("已将 " + student.getName() + " 提拔为阅卷人");
    }

    /**
     * 提拔玩家为管理员 (→level 3)
     * 需要 Owner(level=4)
     */
    @Transactional
    @PostMapping("/studentToHelper/{studentId}")
    public Result studentToHelper(@PathVariable Integer studentId) {
        Admin operator = getOperatorAdmin();
        if (operator == null || operator.getLevel() == null || operator.getLevel() < 4) {
            return Result.error("权限不足，需要 Owner 权限才能提拔为管理员");
        }

        Student student = studentMapper.selectById(studentId);
        if (ObjectUtil.isNull(student)) {
            return Result.error("玩家不存在");
        }

        Admin admin = new Admin();
        admin.setUsername(student.getUsername());
        admin.setPassword(student.getPassword());
        admin.setName(student.getName());
        admin.setPhone(student.getPhone());
        admin.setEmail(student.getEmail());
        admin.setAvatar(student.getAvatar());
        admin.setRole("ADMIN");
        admin.setLevel(3);
        adminMapper.insert(admin);

        student.setStatus("PROMOTED");
        studentMapper.updateById(student);

        return Result.success("已将 " + student.getName() + " 提拔为管理员");
    }

    /**
     * 提拔阅卷人为管理员 (→level 3)
     * 需要 Owner(level=4)
     */
    @Transactional
    @PostMapping("/examinerToHelper/{examinerId}")
    public Result examinerToHelper(@PathVariable Integer examinerId) {
        Admin operator = getOperatorAdmin();
        if (operator == null || operator.getLevel() == null || operator.getLevel() < 4) {
            return Result.error("权限不足，需要 Owner 权限才能提拔为管理员");
        }

        Examiner examiner = examinerMapper.selectById(examinerId);
        if (ObjectUtil.isNull(examiner)) {
            return Result.error("阅卷人不存在");
        }

        Admin admin = new Admin();
        admin.setUsername(examiner.getUsername());
        admin.setPassword(examiner.getPassword());
        admin.setName(examiner.getName());
        admin.setPhone(examiner.getPhone());
        admin.setEmail(examiner.getEmail());
        admin.setAvatar(examiner.getAvatar());
        admin.setRole("ADMIN");
        admin.setLevel(3);
        adminMapper.insert(admin);

        examinerMapper.deleteById(examinerId);

        return Result.success("已将 " + examiner.getName() + " 提拔为管理员");
    }

    /**
     * 获取当前登录的管理员（含level）
     */
    private Admin getOperatorAdmin() {
        try {
            com.example.entity.Account current = TokenUtils.getCurrentUser();
            if (current != null && ("OWNER".equals(current.getRole()) || "ADMIN".equals(current.getRole()))) {
                return adminMapper.selectById(current.getId());
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
