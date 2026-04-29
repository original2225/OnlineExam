package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.InvitationCode;
import com.example.service.InvitationCodeService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 邀请码控制器
 */
@RestController
@RequestMapping("/invitationCode")
public class InvitationCodeController {

    @Resource
    private InvitationCodeService invitationCodeService;

    /**
     * 批量生成邀请码
     */
    @PostMapping("/generate")
    public Result generate(@RequestBody Map<String, Object> params) {
        Integer count = (Integer) params.get("count");
        Integer daysValid = (Integer) params.get("daysValid");
        String remark = (String) params.get("remark");
        String targetRole = "USER";
        Account currentUser = TokenUtils.getCurrentUser();
        Integer createdBy = currentUser != null ? currentUser.getId() : null;

        if (count == null || count <= 0) {
            return Result.error("请输入有效的生成数量");
        }

        if (count > 100) {
            return Result.error("单次最多生成100个邀请码");
        }

        List<String> codes = invitationCodeService.batchGenerate(count, daysValid, createdBy, remark, targetRole);
        return Result.success(codes);
    }

    /**
     * 查询所有邀请码
     */
    @GetMapping("/selectAll")
    public Result selectAll(InvitationCode invitationCode) {
        List<InvitationCode> list = invitationCodeService.selectAll(invitationCode);
        return Result.success(list);
    }

    /**
     * 分页查询邀请码
     */
    @GetMapping("/selectPage")
    public Result selectPage(InvitationCode invitationCode,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<InvitationCode> pageInfo = invitationCodeService.selectPage(invitationCode, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        Map<String, Object> stats = invitationCodeService.getStatistics();
        return Result.success(stats);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        InvitationCode invitationCode = invitationCodeService.selectById(id);
        return Result.success(invitationCode);
    }

    /**
     * 删除邀请码
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        invitationCodeService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除邀请码
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        invitationCodeService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 删除所有未使用的邀请码
     */
    @DeleteMapping("/deleteAllUnused")
    public Result deleteAllUnused() {
        int count = invitationCodeService.deleteAllUnused();
        return Result.success(count);
    }
}
