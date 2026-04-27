package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Tutorial;
import com.example.service.TutorialService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 例题讲解教程接口
 */
@RestController
@RequestMapping("/tutorial")
public class TutorialController {

    @Resource
    private TutorialService tutorialService;

    /**
     * 新增教程（需要 HELPER 及以上权限）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Tutorial tutorial) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        if (!hasPermission(currentUser)) {
            return Result.error("403", "权限不足，需要 HELPER 及以上权限");
        }
        tutorial.setCreatorId(currentUser.getId());
        tutorial.setCreatorName(currentUser.getUsername());
        tutorial.setCreatorRole(currentUser.getRole());
        if (tutorial.getStatus() == null) {
            tutorial.setStatus("active");
        }
        if (tutorial.getViewCount() == null) {
            tutorial.setViewCount(0);
        }
        tutorialService.add(tutorial);
        return Result.success();
    }

    /**
     * 修改教程（需要 HELPER 及以上权限）
     */
    @PutMapping("/update")
    public Result update(@RequestBody Tutorial tutorial) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !hasPermission(currentUser)) {
            return Result.error("403", "权限不足，需要 HELPER 及以上权限");
        }
        tutorialService.updateById(tutorial);
        return Result.success();
    }

    /**
     * 删除教程（需要 HELPER 及以上权限）
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !hasPermission(currentUser)) {
            return Result.error("403", "权限不足，需要 HELPER 及以上权限");
        }
        tutorialService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除教程（需要 HELPER 及以上权限）
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !hasPermission(currentUser)) {
            return Result.error("403", "权限不足，需要 HELPER 及以上权限");
        }
        tutorialService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 查询单个教程
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Tutorial tutorial = tutorialService.selectById(id);
        return Result.success(tutorial);
    }

    /**
     * 查询所有教程
     */
    @GetMapping("/selectAll")
    public Result selectAll(Tutorial tutorial) {
        List<Tutorial> list = tutorialService.selectAll(tutorial);
        return Result.success(list);
    }

    /**
     * 分页查询教程
     */
    @GetMapping("/selectPage")
    public Result selectPage(Tutorial tutorial,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Tutorial> pageInfo = tutorialService.selectPage(tutorial, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 浏览量 +1
     */
    @PutMapping("/incrementView/{id}")
    public Result incrementView(@PathVariable Integer id) {
        tutorialService.incrementViewCount(id);
        return Result.success();
    }

    /**
     * 检查权限：HELPER/ADMIN/OWNER 可操作
     */
    private boolean hasPermission(Account user) {
        String role = user.getRole();
        return "OWNER".equals(role) || "ADMIN".equals(role) || "HELPER".equals(role);
    }
}
