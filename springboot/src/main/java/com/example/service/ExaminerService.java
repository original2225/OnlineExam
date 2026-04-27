package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Examiner;
import com.example.exception.CustomException;
import com.example.mapper.ExaminerMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 阅卷人业务层方法
 */
@Service
public class ExaminerService {

    @Resource
    private ExaminerMapper examinerMapper;

    public void add(Examiner examiner) {
        Examiner dbExaminer = examinerMapper.selectByUsername(examiner.getUsername());
        if (ObjectUtil.isNotNull(dbExaminer)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(examiner.getPassword())) {
            examiner.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (ObjectUtil.isEmpty(examiner.getName())) {
            examiner.setName(examiner.getUsername());
        }
        examiner.setRole(RoleEnum.HELPER.name());
        examinerMapper.insert(examiner);
    }

    public void updateById(Examiner examiner) {
        examinerMapper.updateById(examiner);
    }

    public void deleteById(Integer id) {
        examinerMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            examinerMapper.deleteById(id);
        }
    }

    public Examiner selectById(Integer id) {
        return examinerMapper.selectById(id);
    }

    public List<Examiner> selectAll(Examiner examiner) {
        return examinerMapper.selectAll(examiner);
    }

    public PageInfo<Examiner> selectPage(Examiner examiner, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Examiner> list = examinerMapper.selectAll(examiner);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public Examiner login(Account account) {
        Examiner dbExaminer = examinerMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbExaminer)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!dbExaminer.getPassword().equals(account.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token
        String token = TokenUtils.createToken(dbExaminer.getId() + "-" + dbExaminer.getRole(), dbExaminer.getPassword());
        dbExaminer.setToken(token);
        return dbExaminer;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Examiner dbExaminer = examinerMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbExaminer)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbExaminer.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbExaminer.setPassword(account.getNewPassword());
        examinerMapper.updateById(dbExaminer);
    }

}
