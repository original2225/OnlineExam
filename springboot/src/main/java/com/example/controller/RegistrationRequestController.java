package com.example.controller;

import com.example.common.Result;
import com.example.entity.RegistrationRequest;
import com.example.entity.Student;
import com.example.service.RegistrationRequestService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 注册申请控制器
 */
@RestController
@RequestMapping("/registration")
public class RegistrationRequestController {

    @Resource
    private RegistrationRequestService registrationRequestService;

    /**
     * 玩家提交注册申请（公开接口，无需登录）
     * 有邀请码：直接注册成功
     * 无邀请码：创建待审批申请
     */
    @PostMapping("/register")
    public Result submitRequest(@RequestBody RegistrationRequest request) {
        Object account = registrationRequestService.submitRequest(request);
        if (account != null) {
            // 有邀请码，直接注册成功
            return Result.success("注册成功！您现在可以登录了");
        } else {
            // 无邀请码，需要等待审批
            return Result.success("注册申请已提交，请等待管理员审批");
        }
    }

    /**
     * 管理员审批通过
     */
    @PostMapping("/approve")
    public Result approveRequest(@RequestParam Integer id) {
        // TODO: 从JWT中获取管理员ID，这里暂时使用固定值
        Integer adminId = 1;
        registrationRequestService.approveRequest(id, adminId);
        return Result.success("申请已通过，玩家账号已创建");
    }

    /**
     * 管理员拒绝申请
     */
    @PostMapping("/reject")
    public Result rejectRequest(@RequestParam Integer id, @RequestParam(required = false) String reason) {
        // TODO: 从JWT中获取管理员ID，这里暂时使用固定值
        Integer adminId = 1;
        registrationRequestService.rejectRequest(id, adminId, reason);
        return Result.success("申请已拒绝");
    }

    /**
     * 查询所有申请（管理员）
     */
    @GetMapping("/selectAll")
    public Result selectAll(RegistrationRequest request) {
        List<RegistrationRequest> list = registrationRequestService.selectAll(request);
        return Result.success(list);
    }

    /**
     * 分页查询申请（管理员）
     */
    @GetMapping("/selectPage")
    public Result selectPage(RegistrationRequest request,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<RegistrationRequest> pageInfo = registrationRequestService.selectPage(request, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        RegistrationRequest request = registrationRequestService.selectById(id);
        return Result.success(request);
    }

    /**
     * 删除申请记录
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        registrationRequestService.deleteById(id);
        return Result.success();
    }

    /**
     * 获取待审批数量
     */
    @GetMapping("/pending")
    public Result pending() {
        int count = registrationRequestService.countPending();
        return Result.success(count);
    }
}
