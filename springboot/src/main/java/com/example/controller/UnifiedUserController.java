package com.example.controller;

import com.example.common.Result;
import com.example.entity.UnifiedUser;
import com.example.mapper.UnifiedUserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一用户列表接口
 * 合并 admin、examiner、student 三张表，按编号排序
 */
@RestController
@RequestMapping("/unifiedUser")
public class UnifiedUserController {

    @Resource
    private UnifiedUserMapper unifiedUserMapper;

    /**
     * 查询所有用户（统一列表，按编号排序）
     */
    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String role) {
        List<UnifiedUser> list = unifiedUserMapper.selectAll();
        // 前端筛选
        if (name != null && !name.isEmpty()) {
            list = list.stream()
                    .filter(u -> u.getName() != null && u.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (role != null && !role.isEmpty()) {
            list = list.stream()
                    .filter(u -> role.equals(u.getSource()))
                    .collect(Collectors.toList());
        }
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String role,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "15") Integer pageSize) {
        List<UnifiedUser> list = unifiedUserMapper.selectAll();
        if (name != null && !name.isEmpty()) {
            list = list.stream()
                    .filter(u -> u.getName() != null && u.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (role != null && !role.isEmpty()) {
            list = list.stream()
                    .filter(u -> role.equals(u.getSource()))
                    .collect(Collectors.toList());
        }
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<UnifiedUser> pageList = start < total ? list.subList(start, end) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("total", total);
        return Result.success(result);
    }

    /**
     * 统计各角色人数
     */
    @GetMapping("/count")
    public Result count() {
        List<UnifiedUser> all = unifiedUserMapper.selectAll();
        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", all.size());
        counts.put("admins", (int) all.stream().filter(u -> "OWNER".equals(u.getSource()) || "ADMIN".equals(u.getSource())).count());
        counts.put("examiners", (int) all.stream().filter(u -> "HELPER".equals(u.getSource())).count());
        counts.put("users", (int) all.stream().filter(u -> "USER".equals(u.getSource())).count());
        return Result.success(counts);
    }
}
