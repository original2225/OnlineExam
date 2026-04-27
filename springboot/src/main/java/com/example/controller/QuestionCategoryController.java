package com.example.controller;

import com.example.common.Result;
import com.example.entity.QuestionCategory;
import com.example.service.QuestionCategoryService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mapper.QuestionCategoryMapper;
import com.example.mapper.QuestionMapper;

/**
 * 题目分类前端请求接口
 */
@RestController
@RequestMapping("/questionCategory")
public class QuestionCategoryController {

    @Resource
    private QuestionCategoryService questionCategoryService;

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    @Resource
    private QuestionMapper questionMapper;

    @GetMapping("/questionStats")
    public Result questionStats() {
        try {
            List<QuestionCategory> all = questionCategoryMapper.selectAll(new QuestionCategory());
            Map<String, Integer> countMap = new HashMap<>();
            for (QuestionCategory cat : all) {
                try {
                    int count = questionMapper.countByCategoryId(cat.getId());
                    countMap.put(String.valueOf(cat.getId()), count);
                } catch (Exception e) {
                    countMap.put(String.valueOf(cat.getId()), 0);
                }
            }
            return Result.success(countMap);
        } catch (Exception e) {
            return Result.error("统计失败: " + e.getMessage());
        }
    }

    /**
     * 初始化完整学科树
     * 顶级学科：我的世界进服审核题目、小学算术、看图写话
     * 子分类按学科展开
     */
    @PostMapping("/initFullDefaults")
    public Result initFullDefaults() {
        // 定义学科结构：{学科名, 图标, 描述, 子分类[]}
        Object[][] subjects = {
                {"我的世界进服审核题目", "minecraft", "Minecraft 服务器进服审核相关题目", new String[]{"红石工程", "建筑设计", "后勤管理", "基础知识"}},
                {"小学算术", "math", "小学数学基础运算与应用题", new String[]{"加减运算", "乘除运算", "应用题"}},
                {"看图写话", "writing", "看图理解与短文写作练习", new String[]{"词语理解", "短文写作"}}
        };

        for (Object[] subject : subjects) {
            String subjectName = (String) subject[0];
            String icon = (String) subject[1];
            String desc = (String) subject[2];
            String[] children = (String[]) subject[3];

            // 检查学科是否已存在
            QuestionCategory query = new QuestionCategory();
            query.setName(subjectName);
            List<QuestionCategory> existing = questionCategoryMapper.selectAll(query);
            QuestionCategory parent;
            if (existing == null || existing.isEmpty()) {
                parent = new QuestionCategory();
                parent.setName(subjectName);
                parent.setIcon(icon);
                parent.setDescription(desc);
                parent.setStatus("active");
                questionCategoryMapper.insert(parent);
            } else {
                parent = existing.get(0);
                // 更新 icon 和 description
                parent.setIcon(icon);
                parent.setDescription(desc);
                questionCategoryMapper.updateById(parent);
            }

            // 创建子分类
            for (String childName : children) {
                QuestionCategory childQuery = new QuestionCategory();
                childQuery.setName(childName);
                childQuery.setParentId(parent.getId());
                List<QuestionCategory> childExisting = questionCategoryMapper.selectByParentId(parent.getId());
                boolean exists = childExisting != null && childExisting.stream().anyMatch(c -> c.getName().equals(childName));
                if (!exists) {
                    QuestionCategory child = new QuestionCategory();
                    child.setName(childName);
                    child.setParentId(parent.getId());
                    child.setStatus("active");
                    questionCategoryMapper.insert(child);
                }
            }
        }
        return Result.success("完整学科树已初始化");
    }

    /**
     * 获取分类树形结构
     */
    @GetMapping("/selectTree")
    public Result selectTree() {
        List<QuestionCategory> all = questionCategoryMapper.selectAll(new QuestionCategory());
        List<QuestionCategory> tree = new ArrayList<>();
        for (QuestionCategory cat : all) {
            if (cat.getParentId() == null) {
                cat.setChildren(new ArrayList<>());
                tree.add(cat);
            }
        }
        for (QuestionCategory cat : all) {
            if (cat.getParentId() != null) {
                for (QuestionCategory parent : tree) {
                    if (parent.getId().equals(cat.getParentId())) {
                        parent.getChildren().add(cat);
                        break;
                    }
                }
            }
        }
        return Result.success(tree);
    }

    /**
     * 获取某学科的子分类
     */
    @GetMapping("/selectByParentId/{parentId}")
    public Result selectByParentId(@PathVariable Integer parentId) {
        List<QuestionCategory> list = questionCategoryMapper.selectByParentId(parentId);
        return Result.success(list);
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody QuestionCategory category) {
        questionCategoryService.add(category);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody QuestionCategory category) {
        questionCategoryService.updateById(category);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionCategoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        questionCategoryService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        QuestionCategory category = questionCategoryService.selectById(id);
        return Result.success(category);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(QuestionCategory category) {
        List<QuestionCategory> list = questionCategoryService.selectAll(category);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(QuestionCategory category,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<QuestionCategory> pageInfo = questionCategoryService.selectPage(category, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
