package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.InvitationCode;
import com.example.exception.CustomException;
import com.example.mapper.InvitationCodeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 邀请码服务
 */
@Service
public class InvitationCodeService {

    @Resource
    private InvitationCodeMapper invitationCodeMapper;

    /**
     * 批量生成邀请码
     */
    @Transactional
    public List<String> batchGenerate(Integer count, Integer daysValid, Integer createdBy, String remark, String targetRole) {
        List<InvitationCode> codes = new ArrayList<>();
        List<String> codeStrings = new ArrayList<>();
        LocalDateTime expireTime = null;

        if (daysValid != null && daysValid > 0) {
            expireTime = LocalDateTime.now().plusDays(daysValid);
        }

        for (int i = 0; i < count; i++) {
            String codeStr;
            do {
                // 生成8位随机码（大写字母+数字）
                codeStr = RandomUtil.randomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 8);
            } while (ObjectUtil.isNotNull(invitationCodeMapper.selectByCode(codeStr))); // 确保唯一

            InvitationCode invitationCode = new InvitationCode();
            invitationCode.setCode(codeStr);
            invitationCode.setStatus("UNUSED");
            invitationCode.setCreateTime(LocalDateTime.now());
            invitationCode.setCreatedBy(createdBy);
            invitationCode.setExpireTime(expireTime);
            invitationCode.setRemark(remark);
            invitationCode.setTargetRole(targetRole);

            codes.add(invitationCode);
            codeStrings.add(codeStr);
        }

        // 批量插入
        invitationCodeMapper.batchInsert(codes);

        return codeStrings;
    }

    /**
     * 验证邀请码
     */
    @Transactional
    public InvitationCode validateAndUse(String code, Integer userId, String username) {
        if (ObjectUtil.isEmpty(code)) {
            return null;
        }

        InvitationCode invitationCode = invitationCodeMapper.selectByCode(code);
        if (ObjectUtil.isNull(invitationCode)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "邀请码不存在");
        }

        if (!"UNUSED".equals(invitationCode.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "邀请码已被使用");
        }

        // 检查是否过期
        if (invitationCode.getExpireTime() != null &&
            invitationCode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "邀请码已过期");
        }

        // 标记为已使用
        invitationCodeMapper.updateToUsed(
                invitationCode.getId(),
                userId,
                username,
                LocalDateTime.now()
        );

        return invitationCode;
    }

    /**
     * 查询所有邀请码
     */
    public List<InvitationCode> selectAll(InvitationCode invitationCode) {
        return invitationCodeMapper.selectAll(invitationCode);
    }

    /**
     * 分页查询邀请码
     */
    public PageInfo<InvitationCode> selectPage(InvitationCode invitationCode, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<InvitationCode> list = invitationCodeMapper.selectAll(invitationCode);
        return PageInfo.of(list);
    }

    /**
     * 根据ID查询
     */
    public InvitationCode selectById(Integer id) {
        return invitationCodeMapper.selectById(id);
    }

    /**
     * 删除邀请码
     */
    public void deleteById(Integer id) {
        invitationCodeMapper.deleteById(id);
    }

    /**
     * 批量删除邀请码
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            invitationCodeMapper.deleteById(id);
        }
    }

    /**
     * 删除所有未使用的邀请码
     */
    @Transactional
    public int deleteAllUnused() {
        return invitationCodeMapper.deleteAllUnused();
    }

    /**
     * 获取统计信息
     */
    public java.util.Map<String, Object> getStatistics() {
        List<InvitationCode> allCodes = invitationCodeMapper.selectAll(new InvitationCode());

        long total = allCodes.size();
        long unused = allCodes.stream().filter(c -> "UNUSED".equals(c.getStatus())).count();
        long used = allCodes.stream().filter(c -> "USED".equals(c.getStatus())).count();
        long studentUnused = allCodes.stream().filter(c -> "UNUSED".equals(c.getStatus()) && !"HELPER".equals(c.getTargetRole())).count();
        long examinerUnused = allCodes.stream().filter(c -> "UNUSED".equals(c.getStatus()) && "HELPER".equals(c.getTargetRole())).count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("unused", unused);
        stats.put("used", used);
        stats.put("studentUnused", studentUnused);
        stats.put("examinerUnused", examinerUnused);

        return stats;
    }
}
