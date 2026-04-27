package com.example.controller;

import com.example.common.Result;
import com.example.entity.Question;
import com.example.entity.QuestionCategory;
import com.example.mapper.QuestionCategoryMapper;
import com.example.service.QuestionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 题目前端请求接口
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Question question) {
        questionService.add(question);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Question question) {
        questionService.updateById(question);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        questionService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        questionService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Question question = questionService.selectById(id);
        return Result.success(question);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Question question) {
        List<Question> list = questionService.selectAll(question);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Question question,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Question> pageInfo = questionService.selectPage(question, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 根据试卷ID查询题目
     */
    @GetMapping("/selectByPaperId/{paperId}")
    public Result selectByPaperId(@PathVariable Integer paperId) {
        List<Question> list = questionService.selectByPaperId(paperId);
        return Result.success(list);
    }

    /**
     * 初始化进服审核示例题目数据
     */
    @PostMapping("/initSample")
    public Result initSample() {
        Question check = new Question();
        List<Question> existing = questionService.selectAll(check);
        if (!existing.isEmpty()) {
            return Result.error("题库中已有数据，无需初始化");
        }

        Map<String, Integer> categories = ensureAuditCategories();

        addSingleQuestion(categories.get("普通(见习)审核"), "普通(见习)审核中，进入生电服务器前最应该确认哪项？",
                Map.of("A", "服务器版本、规则和准入要求", "B", "材质包颜色", "C", "个人皮肤稀有度", "D", "聊天昵称长度"),
                "A", "进服审核先确认版本、规则、权限边界和服务器基本要求。", "easy", "2");
        addMultipleQuestion(categories.get("红石审核"), "设计大型红石机器前，需要提前说明哪些内容？",
                Map.of("A", "用途与产能", "B", "卡服风险", "C", "区块加载方式", "D", "机器外观颜色"),
                "ABC", "生电服务器关注用途、性能影响、加载方式和维护成本。", "medium", "5");
        addEssayQuestion(categories.get("建筑审核"), "请说明你会如何让公共建筑同时满足美观、可维护和服务器风格统一。",
                "要点：明确风格参考、控制体量比例、选材统一、预留维护空间、避免遮挡公共线路。", "hard", "10");
        addEssayQuestion(categories.get("后期审核"), "请说明你能承担哪些后期工作，以及如何记录工程进度和物资消耗。",
                "要点：物资统计、仓储整理、施工收尾、文档记录、截图归档、异常反馈。", "medium", "8");

        return Result.success("已成功初始化4道进服审核示例题目");
    }

    private Map<String, Integer> ensureAuditCategories() {
        QuestionCategory query = new QuestionCategory();
        query.setName("Minecraft Java 生电服务器进服审核");
        List<QuestionCategory> existing = questionCategoryMapper.selectAll(query);
        QuestionCategory parent;
        if (existing.isEmpty()) {
            parent = new QuestionCategory();
            parent.setName("Minecraft Java 生电服务器进服审核");
            parent.setDescription("面向生电服务器成员准入的四项审核题库");
            parent.setIcon("minecraft");
            parent.setStatus("active");
            questionCategoryMapper.insert(parent);
        } else {
            parent = existing.get(0);
        }

        Map<String, Integer> ids = new LinkedHashMap<>();
        for (String name : List.of("建筑审核", "后期审核", "红石审核", "普通(见习)审核")) {
            List<QuestionCategory> children = questionCategoryMapper.selectByParentId(parent.getId());
            QuestionCategory category = children == null ? null : children.stream().filter(c -> name.equals(c.getName())).findFirst().orElse(null);
            if (category == null) {
                category = new QuestionCategory();
                category.setName(name);
                category.setParentId(parent.getId());
                category.setStatus("active");
                questionCategoryMapper.insert(category);
            }
            ids.put(name, category.getId());
        }
        return ids;
    }

    private void addSingleQuestion(Integer categoryId, String content, Map<String, String> options, String answer,
                                   String analysis, String difficulty, String score) {
        Question question = new Question();
        question.setCategoryId(categoryId);
        question.setType("single");
        question.setContent(content);
        question.setOptions(options);
        question.setAnswer(answer);
        question.setAnalysis(analysis);
        question.setDifficulty(difficulty);
        question.setScore(new BigDecimal(score));
        question.setStatus("active");
        questionService.add(question);
    }

    private void addMultipleQuestion(Integer categoryId, String content, Map<String, String> options, String answer,
                                     String analysis, String difficulty, String score) {
        Question question = new Question();
        question.setCategoryId(categoryId);
        question.setType("multiple");
        question.setContent(content);
        question.setOptions(options);
        question.setAnswer(answer);
        question.setAnalysis(analysis);
        question.setDifficulty(difficulty);
        question.setScore(new BigDecimal(score));
        question.setStatus("active");
        questionService.add(question);
    }

    private void addEssayQuestion(Integer categoryId, String content, String answer,
                                  String difficulty, String score) {
        Question question = new Question();
        question.setCategoryId(categoryId);
        question.setType("essay");
        question.setContent(content);
        question.setAnswer(answer);
        question.setAnalysis(answer);
        question.setDifficulty(difficulty);
        question.setScore(new BigDecimal(score));
        question.setStatus("active");
        questionService.add(question);
    }


}