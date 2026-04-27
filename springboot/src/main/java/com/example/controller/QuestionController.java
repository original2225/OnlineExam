package com.example.controller;

import com.example.common.Result;
import com.example.entity.Exam;
import com.example.entity.ExamPaper;
import com.example.entity.Question;
import com.example.entity.QuestionCategory;
import com.example.mapper.QuestionCategoryMapper;
import com.example.service.ExamPaperService;
import com.example.service.ExamService;
import com.example.service.QuestionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private ExamService examService;

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
     * 初始化示例题目数据
     */
    @PostMapping("/initSample")
    public Result initSample() {
        // 检查是否已有数据
        Question check = new Question();
        List<Question> existing = questionService.selectAll(check);
        if (!existing.isEmpty()) {
            return Result.error("题库中已有数据，无需初始化");
        }

        // 创建分类
        QuestionCategory mathCat = new QuestionCategory();
        mathCat.setName("小学算术");
        mathCat.setDescription("小学基础算术题");
        mathCat.setStatus("active");
        questionCategoryMapper.insert(mathCat);

        // 示例题目
        // 1. 单选题：加法
        Question q1 = new Question();
        q1.setCategoryId(mathCat.getId());
        q1.setType("single");
        q1.setContent("小明有3个苹果，小红给了他5个苹果，请问小明现在有多少个苹果？");
        Map<String, String> opts1 = new LinkedHashMap<>();
        opts1.put("A", "6个");
        opts1.put("B", "7个");
        opts1.put("C", "8个");
        opts1.put("D", "9个");
        q1.setOptions(opts1);
        q1.setAnswer("C");
        q1.setAnalysis("3 + 5 = 8，所以小明现在有8个苹果。");
        q1.setDifficulty("easy");
        q1.setScore(new BigDecimal("2"));
        q1.setStatus("active");
        questionService.add(q1);

        // 2. 单选题：减法
        Question q2 = new Question();
        q2.setCategoryId(mathCat.getId());
        q2.setType("single");
        q2.setContent("桌上有12块糖，被吃掉了4块，还剩多少块？");
        Map<String, String> opts2 = new LinkedHashMap<>();
        opts2.put("A", "6块");
        opts2.put("B", "7块");
        opts2.put("C", "8块");
        opts2.put("D", "9块");
        q2.setOptions(opts2);
        q2.setAnswer("C");
        q2.setAnalysis("12 - 4 = 8，所以还剩8块。");
        q2.setDifficulty("easy");
        q2.setScore(new BigDecimal("2"));
        q2.setStatus("active");
        questionService.add(q2);

        // 3. 判断题
        Question q3 = new Question();
        q3.setCategoryId(mathCat.getId());
        q3.setType("judge");
        q3.setContent("2 + 2 = 5 这个等式是正确的。");
        q3.setAnswer("错");
        q3.setAnalysis("2 + 2 = 4，所以这个等式是错误的。");
        q3.setDifficulty("easy");
        q3.setScore(new BigDecimal("2"));
        q3.setStatus("active");
        questionService.add(q3);

        // 4. 填空题
        Question q4 = new Question();
        q4.setCategoryId(mathCat.getId());
        q4.setType("fillin");
        q4.setContent("6 × 7 = ___，8 × 9 = ___");
        q4.setAnswer("42|||72");
        q4.setAnalysis("乘法口诀：六七四十二，八九七十二。");
        q4.setDifficulty("medium");
        q4.setScore(new BigDecimal("4"));
        q4.setStatus("active");
        questionService.add(q4);

        // 5. 多选题
        Question q5 = new Question();
        q5.setCategoryId(mathCat.getId());
        q5.setType("multiple");
        q5.setContent("下列哪些算式的结果等于12？（多选）");
        Map<String, String> opts5 = new LinkedHashMap<>();
        opts5.put("A", "3 × 4");
        opts5.put("B", "2 × 5");
        opts5.put("C", "6 + 6");
        opts5.put("D", "15 - 3");
        q5.setOptions(opts5);
        q5.setAnswer("A,C,D");
        q5.setAnalysis("A: 3×4=12 ✓, B: 2×5=10 ✗, C: 6+6=12 ✓, D: 15-3=12 ✓");
        q5.setDifficulty("medium");
        q5.setScore(new BigDecimal("5"));
        q5.setStatus("active");
        questionService.add(q5);

        return Result.success("已成功初始化5道示例题目");
    }

    /**
     * 初始化九九乘法表考试（题目 + 试卷 + 考试）
     */
    @PostMapping("/initMultiplicationExam")
    public Result initMultiplicationExam(@RequestBody(required = false) Map<String, Object> params) {
        // 1. 创建或查找分类
        QuestionCategory queryCat = new QuestionCategory();
        queryCat.setName("九九乘法表");
        List<QuestionCategory> existingCats = questionCategoryMapper.selectAll(queryCat);
        QuestionCategory cat;
        if (!existingCats.isEmpty()) {
            cat = existingCats.get(0);
        } else {
            cat = new QuestionCategory();
            cat.setName("九九乘法表");
            cat.setDescription("九九乘法表专项练习");
            cat.setIcon("math");
            cat.setStatus("active");
            questionCategoryMapper.insert(cat);
        }

        // 2. 生成九九乘法表题目（单选题，i × j，避免重复：只生成 i <= j）
        List<Integer> questionIds = new ArrayList<>();
        Random random = new Random();
        int questionCount = 0;

        for (int i = 1; i <= 9; i++) {
            for (int j = i; j <= 9; j++) {
                int correctAnswer = i * j;
                Question q = new Question();
                q.setCategoryId(cat.getId());
                q.setType("single");
                q.setContent(i + " × " + j + " = ?");

                // 生成4个选项，包含正确答案
                Map<String, String> opts = new LinkedHashMap<>();
                Set<Integer> usedNumbers = new HashSet<>();
                usedNumbers.add(correctAnswer);

                // 生成3个干扰项
                List<Integer> distractors = new ArrayList<>();
                while (distractors.size() < 3) {
                    int fake = correctAnswer + (random.nextInt(21) - 10); // ±10 范围内
                    if (fake < 1 || usedNumbers.contains(fake)) continue;
                    usedNumbers.add(fake);
                    distractors.add(fake);
                }

                // 随机排列选项位置
                List<String> labels = Arrays.asList("A", "B", "C", "D");
                int correctPos = random.nextInt(4);
                int dIdx = 0;
                for (int k = 0; k < 4; k++) {
                    if (k == correctPos) {
                        opts.put(labels.get(k), String.valueOf(correctAnswer));
                    } else {
                        opts.put(labels.get(k), String.valueOf(distractors.get(dIdx++)));
                    }
                }
                q.setOptions(opts);
                q.setAnswer(labels.get(correctPos));
                q.setAnalysis(i + " × " + j + " = " + correctAnswer);
                q.setDifficulty(i <= 3 && j <= 3 ? "easy" : (i <= 6 && j <= 6 ? "medium" : "hard"));
                q.setScore(new BigDecimal("2"));
                q.setStatus("active");
                questionService.add(q);
                questionIds.add(q.getId());
                questionCount++;
            }
        }

        // 3. 创建试卷
        ExamPaper paper = new ExamPaper();
        paper.setName("九九乘法表测验");
        paper.setDescription("九九乘法表专项测验，共" + questionCount + "道单选题，每题2分");
        paper.setTotalScore(new BigDecimal(questionCount * 2));
        paper.setTotalTime(30);
        paper.setPassScore(new BigDecimal("60"));
        paper.setType("manual");
        paper.setStatus("active");
        examPaperService.add(paper);

        // 4. 关联题目到试卷
        examPaperService.addQuestionsToPaper(paper.getId(), questionIds, new BigDecimal("2"));

        // 5. 创建考试
        LocalDateTime startTime = params != null && params.get("startTime") != null
                ? LocalDateTime.parse((String) params.get("startTime"))
                : LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);
        LocalDateTime endTime = params != null && params.get("endTime") != null
                ? LocalDateTime.parse((String) params.get("endTime"))
                : startTime.plusHours(1);
        int duration = params != null && params.get("duration") != null
                ? ((Number) params.get("duration")).intValue()
                : 30;
        String examName = params != null && params.get("examName") != null
                ? (String) params.get("examName")
                : "九九乘法表统一考试";

        Exam exam = new Exam();
        exam.setName(examName);
        exam.setPaperId(paper.getId());
        exam.setDescription("九九乘法表专项统一考试，允许迟到");
        exam.setExamType("scheduled");
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);
        exam.setDuration(duration);
        exam.setAllowLate(true);
        exam.setLateTime(15);
        exam.setStatus("published");
        exam.setBranch(params != null && params.get("branch") != null ? (String) params.get("branch") : "红石见习");
        exam.setAutoPublish(false);
        exam.setEnableRecording(false);
        examService.add(exam);

        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", cat.getId());
        result.put("questionCount", questionCount);
        result.put("paperId", paper.getId());
        result.put("examId", exam.getId());
        result.put("examName", exam.getName());
        result.put("startTime", startTime);
        result.put("endTime", endTime);

        return Result.success(result);
    }

    /**
     * 初始化小学算术题库（加减乘除混合，难度分级）
     */
    @PostMapping("/initPrimaryMath")
    public Result initPrimaryMath() {
        // 1. 创建或查找分类
        QuestionCategory queryCat = new QuestionCategory();
        queryCat.setName("小学算术题库");
        List<QuestionCategory> existingCats = questionCategoryMapper.selectAll(queryCat);
        QuestionCategory cat;
        if (!existingCats.isEmpty()) {
            cat = existingCats.get(0);
        } else {
            cat = new QuestionCategory();
            cat.setName("小学算术题库");
            cat.setDescription("小学算术综合练习题库，含加减乘除");
            cat.setIcon("math");
            cat.setStatus("active");
            questionCategoryMapper.insert(cat);
        }

        Random random = new Random();
        List<Integer> generatedIds = new ArrayList<>();
        int total = 0;

        // === 简单题：10以内加减法 ===
        for (int i = 0; i < 10; i++) {
            int a = random.nextInt(9) + 1;
            int b = random.nextInt(9) + 1;
            int correct = a + b;
            generatedIds.add(addSingleChoice(a + " + " + b + " = ?", correct, cat.getId(), "easy", random, "加法基础"));
            total++;
        }
        for (int i = 0; i < 10; i++) {
            int a = random.nextInt(9) + 1;
            int b = random.nextInt(Math.min(a, 9)) + 1;
            int correct = a - b;
            generatedIds.add(addSingleChoice(a + " - " + b + " = ?", correct, cat.getId(), "easy", random, "减法基础"));
            total++;
        }

        // === 简单题：表内乘除法 ===
        for (int i = 0; i < 8; i++) {
            int table = random.nextInt(8) + 2;
            int multiplier = random.nextInt(9) + 1;
            int correct = table * multiplier;
            generatedIds.add(addSingleChoice(table + " × " + multiplier + " = ?", correct, cat.getId(), "easy", random, "乘法基础"));
            total++;
        }
        for (int i = 0; i < 8; i++) {
            int product = (random.nextInt(8) + 2) * (random.nextInt(9) + 1);
            int divisor = random.nextInt(8) + 2;
            int correct = product / divisor;
            int dividend = correct * divisor;
            generatedIds.add(addSingleChoice(dividend + " ÷ " + divisor + " = ?", correct, cat.getId(), "easy", random, "除法基础"));
            total++;
        }

        // === 中等题：两位数加减法（不进位/进位） ===
        for (int i = 0; i < 8; i++) {
            int a = random.nextInt(90) + 10;
            int b = random.nextInt(90) + 10;
            int correct = a + b;
            generatedIds.add(addSingleChoice(a + " + " + b + " = ?", correct, cat.getId(), "medium", random, "两位数加法"));
            total++;
        }
        for (int i = 0; i < 8; i++) {
            int a = random.nextInt(90) + 10;
            int b = random.nextInt(Math.min(a, 99)) + 1;
            int correct = a - b;
            generatedIds.add(addSingleChoice(a + " - " + b + " = ?", correct, cat.getId(), "medium", random, "两位数减法"));
            total++;
        }

        // === 中等题：两位数乘一位数 ===
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(90) + 10;
            int b = random.nextInt(9) + 1;
            int correct = a * b;
            generatedIds.add(addSingleChoice(a + " × " + b + " = ?", correct, cat.getId(), "medium", random, "两位数乘一位数"));
            total++;
        }

        // === 困难题：三位数加减法 ===
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(900) + 100;
            int b = random.nextInt(900) + 100;
            int correct = a + b;
            generatedIds.add(addSingleChoice(a + " + " + b + " = ?", correct, cat.getId(), "hard", random, "三位数加法"));
            total++;
        }
        for (int i = 0; i < 6; i++) {
            int a = random.nextInt(900) + 100;
            int b = random.nextInt(Math.min(a, 999)) + 1;
            int correct = a - b;
            generatedIds.add(addSingleChoice(a + " - " + b + " = ?", correct, cat.getId(), "hard", random, "三位数减法"));
            total++;
        }

        // === 判断题 ===
        String[] judgeStatements = {
            "8 + 7 = 15", "9 × 9 = 81", "25 - 8 = 17",
            "6 × 7 = 42", "100 ÷ 4 = 25", "13 + 28 = 41",
            "45 - 19 = 24", "8 × 8 = 64", "72 ÷ 9 = 8", "17 + 34 = 50"
        };
        String[] judgeAnswers = {"对", "对", "错", "对", "对", "错", "对", "对", "对", "错"};
        for (int i = 0; i < judgeStatements.length; i++) {
            Question jq = new Question();
            jq.setCategoryId(cat.getId());
            jq.setType("judge");
            jq.setContent(judgeStatements[i] + "（判断对错）");
            jq.setAnswer(judgeAnswers[i]);
            jq.setAnalysis(judgeAnswers[i].equals("对") ? "该等式成立。" : "该等式不成立。");
            jq.setDifficulty(i < 5 ? "easy" : (i < 8 ? "medium" : "hard"));
            jq.setScore(new BigDecimal("2"));
            jq.setStatus("active");
            questionService.add(jq);
            generatedIds.add(jq.getId());
            total++;
        }

        // === 填空题 ===
        String[] fillQuestions = {
            "12 + ___ = 20", "___ + 15 = 30", "8 × 7 = ___",
            "48 ÷ ___ = 6", "___ - 17 = 25", "9 × ___ = 81"
        };
        String[] fillAnswers = {"8", "15", "56", "8", "42", "9"};
        for (int i = 0; i < fillQuestions.length; i++) {
            Question fq = new Question();
            fq.setCategoryId(cat.getId());
            fq.setType("fillin");
            fq.setContent("请在横线上填写正确答案：" + fillQuestions[i]);
            fq.setAnswer(fillAnswers[i]);
            fq.setAnalysis("根据算式运算规律得出答案。");
            fq.setDifficulty(i < 3 ? "easy" : (i < 5 ? "medium" : "hard"));
            fq.setScore(new BigDecimal("3"));
            fq.setStatus("active");
            questionService.add(fq);
            generatedIds.add(fq.getId());
            total++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", cat.getId());
        result.put("totalQuestions", total);
        result.put("questionIds", generatedIds);
        return Result.success(result);
    }

    /**
     * 生成单选题的工具方法
     */
    private int addSingleChoice(String content, int correctAnswer, Integer categoryId,
                                String difficulty, Random random, String topic) {
        Question q = new Question();
        q.setCategoryId(categoryId);
        q.setType("single");
        q.setContent(content);

        Map<String, String> opts = new LinkedHashMap<>();
        Set<Integer> usedNumbers = new HashSet<>();
        usedNumbers.add(correctAnswer);

        List<Integer> distractors = new ArrayList<>();
        int range = correctAnswer > 20 ? 20 : 10;
        while (distractors.size() < 3) {
            int fake = correctAnswer + (random.nextInt(range * 2 + 1) - range);
            if (fake <= 0 || usedNumbers.contains(fake)) continue;
            usedNumbers.add(fake);
            distractors.add(fake);
        }

        List<String> labels = Arrays.asList("A", "B", "C", "D");
        int correctPos = random.nextInt(4);
        int dIdx = 0;
        for (int k = 0; k < 4; k++) {
            if (k == correctPos) {
                opts.put(labels.get(k), String.valueOf(correctAnswer));
            } else {
                opts.put(labels.get(k), String.valueOf(distractors.get(dIdx++)));
            }
        }
        q.setOptions(opts);
        q.setAnswer(labels.get(correctPos));
        q.setAnalysis("根据算术运算规则计算。");
        q.setDifficulty(difficulty);
        q.setScore(difficulty.equals("easy") ? new BigDecimal("2")
                : difficulty.equals("medium") ? new BigDecimal("3") : new BigDecimal("4"));
        q.setStatus("active");
        questionService.add(q);
        return q.getId();
    }

}
