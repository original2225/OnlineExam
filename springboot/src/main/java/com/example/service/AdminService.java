package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.Examiner;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.mapper.ExaminerMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务层方法
 */
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private ExaminerMapper examinerMapper;

    public void add(Admin admin) {
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        if (admin.getLevel() != null && admin.getLevel() >= 4) {
            admin.setRole(RoleEnum.OWNER.name());
        } else {
            admin.setRole(RoleEnum.ADMIN.name());
        }
        adminMapper.insert(admin);
    }

    /**
     * 修改管理员信息
     * 如果 level < 3，则自动将管理员降级为阅卷人（从 admin 表删除，插入 examiner 表）
     */
    @Transactional
    public void updateById(Admin admin) {
        Admin existing = adminMapper.selectById(admin.getId());
        if (existing == null) return;

        if (existing.getLevel() != null && existing.getLevel() >= 3
                && admin.getLevel() != null && admin.getLevel() < 3) {
            // 降级为阅卷人：从 admin 表删除，插入 examiner 表
            Examiner examiner = new Examiner();
            examiner.setUsername(existing.getUsername());
            examiner.setPassword(existing.getPassword());
            examiner.setName(admin.getName() != null ? admin.getName() : existing.getName());
            examiner.setAvatar(admin.getAvatar() != null ? admin.getAvatar() : existing.getAvatar());
            examiner.setPhone(admin.getPhone() != null ? admin.getPhone() : existing.getPhone());
            examiner.setEmail(admin.getEmail() != null ? admin.getEmail() : existing.getEmail());
            examiner.setRole(RoleEnum.HELPER.name());
            examinerMapper.insert(examiner);
            adminMapper.deleteById(admin.getId());
        } else {
            adminMapper.updateById(admin);
        }
    }

    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            adminMapper.deleteById(id);
        }
    }

    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public Admin login(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!dbAdmin.getPassword().equals(account.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token
        String token = TokenUtils.createToken(dbAdmin.getId() + "-" + dbAdmin.getRole(), dbAdmin.getPassword());
        dbAdmin.setToken(token);
        return dbAdmin;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbAdmin.setPassword(account.getNewPassword());
        adminMapper.updateById(dbAdmin);
    }

}
