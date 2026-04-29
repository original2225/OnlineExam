package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.ExaminerService;
import com.example.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private StudentService studentService;

    @Resource
    private ExaminerService examinerService;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello () {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account loginAccount = null;
        if (RoleEnum.OWNER.name().equals(account.getRole()) || RoleEnum.ADMIN.name().equals(account.getRole())) {
            loginAccount = loginManager(account);
        } else if (RoleEnum.USER.name().equals(account.getRole())) {
            loginAccount = studentService.login(account);
        } else if (RoleEnum.HELPER.name().equals(account.getRole())) {
            loginAccount = examinerService.login(account);
        }
        return Result.success(loginAccount);
    }

    private Account loginManager(Account account) {
        try {
            return adminService.login(account);
        } catch (CustomException e) {
            if (!ResultCodeEnum.USER_NOT_EXIST_ERROR.code.equals(e.getCode())) {
                throw e;
            }
            return examinerService.login(account);
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register() {
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if (RoleEnum.OWNER.name().equals(account.getRole()) || RoleEnum.ADMIN.name().equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if (RoleEnum.HELPER.name().equals(account.getRole())) {
            examinerService.updatePassword(account);
        } else if (RoleEnum.USER.name().equals(account.getRole())) {
            studentService.updatePassword(account);
        }
        return Result.success();
    }

}
