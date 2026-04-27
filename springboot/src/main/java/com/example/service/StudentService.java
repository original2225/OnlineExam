package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.StudentMapper;
import com.example.utils.PasswordUtils;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务层方法
 */
@Service
public class StudentService {

    @Resource
    private StudentMapper studentMapper;

    public void add(Student student) {
        Student dbStudent = studentMapper.selectByUsername(student.getUsername());
        if (ObjectUtil.isNotNull(dbStudent)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(student.getPassword())) {
            student.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        student.setPassword(PasswordUtils.encode(student.getPassword()));
        if (ObjectUtil.isEmpty(student.getName())) {
            student.setName(student.getUsername());
        }
        student.setRole(RoleEnum.USER.name());
        studentMapper.insert(student);
    }

    public void updateById(Student student) {
        studentMapper.updateById(student);
    }

    public void deleteById(Integer id) {
        studentMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            studentMapper.deleteById(id);
        }
    }

    public Student selectById(Integer id) {
        return studentMapper.selectById(id);
    }

    public List<Student> selectAll(Student student) {
        return studentMapper.selectAll(student);
    }

    public PageInfo<Student> selectPage(Student student, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Student> list = studentMapper.selectAll(student);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public Student login(Account account) {
        Student dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!PasswordUtils.matches(account.getPassword(), dbStudent.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        if (PasswordUtils.needsUpgrade(dbStudent.getPassword())) {
            dbStudent.setPassword(PasswordUtils.encode(account.getPassword()));
            studentMapper.updateById(dbStudent);
        }
        // 检查账号状态
        if ("PENDING".equals(dbStudent.getStatus()) || "REJECTED".equals(dbStudent.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "您的注册信息正在审核中，请联系管理员及时审核");
        }
        // 生成token
        String token = TokenUtils.createToken(dbStudent.getId() + "-" + dbStudent.getRole());
        dbStudent.setToken(token);
        return dbStudent;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Student dbStudent = studentMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbStudent)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!PasswordUtils.matches(account.getPassword(), dbStudent.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbStudent.setPassword(PasswordUtils.encode(account.getNewPassword()));
        studentMapper.updateById(dbStudent);
    }

}
