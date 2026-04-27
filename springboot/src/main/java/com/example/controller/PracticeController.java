package com.example.controller;

import com.example.common.Result;
import com.example.entity.*;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审核模拟接口
 */
@RestController
@RequestMapping("/practice")
public class PracticeController {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private ExamPaperMapper examPaperMapper;

    @Resource
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Resource
    private ExamMapper examMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    @Resource
    private WrongQuestionMapper wrongQuestionMapper;

    /**
     * 生成练习试卷
     * 根据分类和难度随机抽题，自动创建试卷和考试
     */
    @PostMapping("/generate")
    @Transactional
    public Result generate(@RequestBody Map<String, Object> params) {
        Integer userId = ((Number) params.get("userId")).intValue();
        String userRole = (String) params.get("userRole");
        Integer categoryId = params.get("categoryId") != null ? ((Number) params.get("categoryId")).intValue() : null;
        String difficulty = (String) params.get("difficulty");
        Integer questionCount = params.get("questionCount") != null ? ((Number) params.get("questionCount")).intValue() : 10;
        String practiceName = (String) params.get("name");

        // 查询符合条件的题目
        Question query = new Question();
        query.setCategoryId(categoryId);
        query.setDifficulty(difficulty);
        query.setStatus("active");
        List<Question> allQuestions = questionMapper.selectAll(query);

        if (allQuestions.isEmpty()) {
            return Result.error("没有符合条件的题目");
        }

        // 随机抽题
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.stream()
                .limit(questionCount)
                .collect(Collectors.toList());

        // 创建试卷
        ExamPaper paper = new ExamPaper();
        paper.setName("[练习] " + (practiceName != null ? practiceName : "随机练习"));
        paper.setDescription("自动生成的练习试卷");
        paper.setTotalScore(BigDecimal.valueOf(selected.size() * 10));
        paper.setTotalTime(60);
        paper.setPassScore(BigDecimal.valueOf(selected.size() * 6));
        paper.setType("manual");
        paper.setStatus("active");
        examPaperMapper.insert(paper);

        // 添加题目到试卷
        BigDecimal perScore = BigDecimal.TEN;
        for (int i = 0; i < selected.size(); i++) {
            ExamPaperQuestion epq = new ExamPaperQuestion();
            epq.setPaperId(paper.getId());
            epq.setQuestionId(selected.get(i).getId());
            epq.setQuestionOrder(i + 1);
            epq.setScore(perScore);
            examPaperQuestionMapper.insert(epq);
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建常驻考试
        Exam exam = new Exam();
        exam.setName("[练习] " + (practiceName != null ? practiceName : "随机练习"));
        exam.setPaperId(paper.getId());
        exam.setDescription("审核模拟 - 自动批阅");
        exam.setExamType("permanent");
        exam.setStartTime(now);
        exam.setEndTime(now.plusYears(1));
        exam.setDuration(60);
        exam.setAllowLate(true);
        exam.setCreatedBy(userId);
        exam.setStatus("published");
        exam.setAutoPublish(false);
        exam.setEnableRecording(false);
        examMapper.insert(exam);

        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setExamId(exam.getId());
        record.setPaperId(paper.getId());
        record.setStudentId(userId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("ongoing");
        record.setAutoScore(BigDecimal.ZERO);
        record.setManualScore(BigDecimal.ZERO);
        record.setTotalScore(BigDecimal.ZERO);
        examRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("examId", exam.getId());
        result.put("paperId", paper.getId());
        result.put("questionCount", selected.size());
        return Result.success(result);
    }

    /**
     * 提交练习并自动批阅
     */
    @PostMapping("/submit")
    @Transactional
    public Result submit(@RequestBody Map<String, Object> params) {
        Integer recordId = ((Number) params.get("recordId")).intValue();
        Integer userId = ((Number) params.get("userId")).intValue();
        String userRole = (String) params.get("userRole");
        Boolean saveWrong = params.get("saveWrong") != null ? (Boolean) params.get("saveWrong") : true;

        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !"ongoing".equals(record.getStatus())) {
            return Result.error("记录不存在或已提交");
        }

        // 设置提交时间
        LocalDateTime submitTime = LocalDateTime.now();
        record.setSubmitTime(submitTime);
        long seconds = java.time.Duration.between(record.getStartTime(), submitTime).getSeconds();
        record.setDuration((int) seconds);

        // 自动判分
        List<Question> questions = questionMapper.selectByPaperId(record.getPaperId());
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal autoScore = BigDecimal.ZERO;

        for (Question question : questions) {
            ExamAnswer answer = examAnswerMapper.selectByRecordAndQuestion(recordId, question.getId());
            if (answer == null) continue;

            boolean isCorrect = false;
            BigDecimal questionScore = BigDecimal.ZERO;

            if ("single".equals(question.getType()) || "judge".equals(question.getType())) {
                if (question.getAnswer() != null && answer.getStudentAnswer() != null) {
                    isCorrect = question.getAnswer().equalsIgnoreCase(answer.getStudentAnswer());
                }
                if (isCorrect) questionScore = question.getScore();
            } else if ("multiple".equals(question.getType())) {
                String sa = answer.getStudentAnswer() != null ? sortString(answer.getStudentAnswer()) : "";
                String ca = question.getAnswer() != null ? sortString(question.getAnswer()) : "";
                isCorrect = sa.equalsIgnoreCase(ca);
                if (isCorrect) questionScore = question.getScore();
            }

            answer.setIsCorrect(isCorrect);
            answer.setScore(questionScore);
            examAnswerMapper.updateById(answer);

            totalScore = totalScore.add(question.getScore());
            autoScore = autoScore.add(questionScore);

            // 自动加入错题集
            if (saveWrong && !isCorrect && answer.getStudentAnswer() != null && !answer.getStudentAnswer().isEmpty()) {
                int exists = wrongQuestionMapper.existsByUserAndQuestion(userId, userRole, question.getId());
                if (exists == 0) {
                    WrongQuestion wq = new WrongQuestion();
                    wq.setUserId(userId);
                    wq.setUserRole(userRole);
                    wq.setQuestionId(question.getId());
                    wq.setSource("practice");
                    wq.setSourceId(recordId);
                    wq.setWrongAnswer(answer.getStudentAnswer());
                    wrongQuestionMapper.insert(wq);
                }
            }
        }

        record.setAutoScore(autoScore);
        record.setTotalScore(autoScore);
        record.setManualScore(BigDecimal.ZERO);
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        if (paper != null) {
            record.setIsPass(autoScore.compareTo(paper.getPassScore()) >= 0);
        }
        record.setStatus("completed");
        record.setExamStatus("PENDING");
        examRecordMapper.updateById(record);

        return Result.success(record);
    }

    private String sortString(String str) {
        if (str == null) return "";
        char[] chars = str.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * 审核模拟趋势 — 按日期统计每日练习题数
     */
    @GetMapping("/trend")
    public Result trend(@RequestParam Integer userId, @RequestParam String userRole) {
        List<ExamRecord> allRecords = examRecordMapper.selectAll(new ExamRecord());
        List<ExamRecord> practiceRecords = allRecords.stream()
                .filter(r -> r.getExamName() != null && r.getExamName().startsWith("[练习]") && "completed".equals(r.getStatus()))
                .filter(r -> r.getStudentId() != null && r.getStudentId().equals(userId))
                .toList();

        Map<String, Integer> trend = new LinkedHashMap<>();
        for (ExamRecord r : practiceRecords) {
            if (r.getSubmitTime() != null) {
                String key = r.getSubmitTime().getMonthValue() + "/" + r.getSubmitTime().getDayOfMonth();
                trend.merge(key, 1, Integer::sum);
            }
        }
        return Result.success(trend);
    }

    @GetMapping("/stats")
    public Result stats(@RequestParam Integer userId, @RequestParam(defaultValue = "USER") String userRole) {
        List<ExamRecord> allRecords = examRecordMapper.selectAll(new ExamRecord());
        List<ExamRecord> myPractice = allRecords.stream()
                .filter(r -> r.getExamName() != null && (r.getExamName().startsWith("[练习]") || r.getExamName().startsWith("[挑战]")) && "completed".equals(r.getStatus()))
                .filter(r -> r.getStudentId() != null && r.getStudentId().equals(userId))
                .toList();

        int totalCount = myPractice.size();
        int correctCount = 0;
        int totalQuestions = 0;

        for (ExamRecord r : myPractice) {
            List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(r.getId());
            totalQuestions += answers.size();
            correctCount += (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        }

        double accuracy = totalQuestions > 0 ? Math.round((double) correctCount / totalQuestions * 1000.0) / 10.0 : 0;

        java.time.LocalDateTime weekStart = java.time.LocalDateTime.now().minusDays(7);
        int thisWeek = (int) myPractice.stream().filter(r -> r.getSubmitTime() != null && r.getSubmitTime().isAfter(weekStart)).count();

        java.time.LocalDateTime lastWeekStart = java.time.LocalDateTime.now().minusDays(14);
        int lastWeek = (int) myPractice.stream()
                .filter(r -> r.getSubmitTime() != null && r.getSubmitTime().isAfter(lastWeekStart) && !r.getSubmitTime().isAfter(weekStart))
                .count();

        int weeklyChange = lastWeek > 0 ? (int) Math.round((double)(thisWeek - lastWeek) / lastWeek * 100) : (thisWeek > 0 ? 100 : 0);

        List<ExamRecord> allPractice = allRecords.stream()
                .filter(r -> r.getExamName() != null && (r.getExamName().startsWith("[练习]") || r.getExamName().startsWith("[挑战]")) && "completed".equals(r.getStatus()))
                .toList();

        int totalWrong = 0;
        for (ExamRecord r : myPractice) {
            List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(r.getId());
            totalWrong += (int) answers.stream().filter(a -> Boolean.FALSE.equals(a.getIsCorrect()) && a.getStudentAnswer() != null && !a.getStudentAnswer().isEmpty()).count();
        }

        int wrongInSet = wrongQuestionMapper.countByUser(userId, userRole);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("accuracy", accuracy);
        stats.put("totalQuestions", totalQuestions);
        stats.put("correctCount", correctCount);
        stats.put("thisWeek", thisWeek);
        stats.put("weeklyChange", weeklyChange);
        stats.put("totalWrong", totalWrong);
        stats.put("wrongInSet", wrongInSet);
        return Result.success(stats);
    }

    @GetMapping("/categoryStats")
    public Result categoryStats(@RequestParam Integer userId) {
        List<ExamRecord> allRecords = examRecordMapper.selectAll(new ExamRecord());
        List<ExamRecord> myRecords = allRecords.stream()
                .filter(r -> r.getStudentId() != null && r.getStudentId().equals(userId) && "completed".equals(r.getStatus()))
                .toList();

        Map<String, int[]> typeMap = new LinkedHashMap<>();
        typeMap.put("single", new int[]{0, 0});
        typeMap.put("multiple", new int[]{0, 0});
        typeMap.put("judge", new int[]{0, 0});
        typeMap.put("fillin", new int[]{0, 0});

        String[] typeNames = {"single:单选题", "multiple:多选题", "judge:判断题", "fillin:填空题"};
        for (ExamRecord r : myRecords) {
            List<ExamAnswer> answers = examAnswerMapper.selectByRecordId(r.getId());
            for (ExamAnswer a : answers) {
                if (a.getQuestionId() != null) {
                    try {
                        Question q = questionMapper.selectById(a.getQuestionId());
                        if (q != null && typeMap.containsKey(q.getType())) {
                            typeMap.get(q.getType())[1]++;
                            if (Boolean.TRUE.equals(a.getIsCorrect())) typeMap.get(q.getType())[0]++;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String tn : typeNames) {
            String[] parts = tn.split(":");
            String type = parts[0];
            String name = parts[1];
            int[] data = typeMap.getOrDefault(type, new int[]{0, 0});
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("type", type);
            item.put("total", data[1]);
            item.put("correct", data[0]);
            item.put("accuracy", data[1] > 0 ? Math.round((double) data[0] / data[1] * 1000.0) / 10.0 : 0);
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/calendar")
    public Result calendar(@RequestParam Integer userId, @RequestParam(defaultValue = "30") Integer days) {
        List<ExamRecord> allRecords = examRecordMapper.selectAll(new ExamRecord());
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start = today.minusDays(days - 1);

        Set<String> activeDates = allRecords.stream()
                .filter(r -> r.getStudentId() != null && r.getStudentId().equals(userId) && "completed".equals(r.getStatus()) && r.getSubmitTime() != null)
                .map(r -> r.getSubmitTime().toLocalDate().toString())
                .collect(Collectors.toSet());

        List<Map<String, Object>> calendar = new ArrayList<>();
        for (java.time.LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", d.getMonthValue() + "/" + d.getDayOfMonth());
            item.put("fullDate", d.toString());
            item.put("hasActivity", activeDates.contains(d.toString()));
            item.put("isToday", d.equals(today));
            calendar.add(item);
        }
        return Result.success(calendar);
    }

    /**
     * 练习排行榜 — 按练习次数和平均分排序
     */
    @GetMapping("/leaderboard")
    public Result leaderboard(@RequestParam(defaultValue = "week") String period) {
        List<ExamRecord> allRecords = examRecordMapper.selectAll(new ExamRecord());
        // 筛选练习记录
        List<ExamRecord> practiceRecords = allRecords.stream()
                .filter(r -> r.getExamName() != null && r.getExamName().startsWith("[练习]") && "completed".equals(r.getStatus()))
                .toList();

        // 按玩家分组统计
        Map<Integer, List<ExamRecord>> grouped = new HashMap<>();
        for (ExamRecord r : practiceRecords) {
            grouped.computeIfAbsent(r.getStudentId(), k -> new ArrayList<>()).add(r);
        }

        List<Map<String, Object>> board = new ArrayList<>();
        for (Map.Entry<Integer, List<ExamRecord>> entry : grouped.entrySet()) {
            List<ExamRecord> records = entry.getValue();
            int count = records.size();
            double avgScore = records.stream()
                    .mapToDouble(r -> r.getTotalScore() != null ? r.getTotalScore().doubleValue() : 0)
                    .average().orElse(0);
            Map<String, Object> item = new HashMap<>();
            item.put("userId", entry.getKey());
            item.put("studentName", records.get(0).getStudentName());
            item.put("practiceCount", count);
            item.put("avgScore", Math.round(avgScore * 10) / 10.0);
            item.put("id", entry.getKey());
            item.put("name", records.get(0).getStudentName());
            board.add(item);
        }
        board.sort((a, b) -> {
            int c = ((Number) b.get("practiceCount")).intValue() - ((Number) a.get("practiceCount")).intValue();
            if (c != 0) return c;
            return ((Double) b.get("avgScore")).compareTo((Double) a.get("avgScore"));
        });
        return Result.success(board.stream().limit(20).toList());
    }

    /**
     * 挑战模式 — 生成与指定用户相同数量的练习题
     */
    @PostMapping("/challenge")
    @Transactional
    public Result challenge(@RequestBody Map<String, Object> params) {
        Integer userId = ((Number) params.get("userId")).intValue();
        String userRole = (String) params.get("userRole");
        Integer categoryId = params.get("categoryId") != null ? ((Number) params.get("categoryId")).intValue() : null;

        Question query = new Question();
        query.setCategoryId(categoryId);
        query.setStatus("active");
        List<Question> allQuestions = questionMapper.selectAll(query);

        if (allQuestions.isEmpty()) {
            return Result.error("没有符合条件的题目");
        }

        Collections.shuffle(allQuestions);
        int questionCount = Math.min(5, allQuestions.size()); // 挑战模式5题

        List<Question> selected = allQuestions.stream().limit(questionCount).collect(Collectors.toList());

        // 创建练习
        ExamPaper paper = new ExamPaper();
        paper.setName("[挑战] 限时对决");
        paper.setDescription("挑战模式 - 速度与正确率的较量");
        paper.setTotalScore(BigDecimal.valueOf(selected.size() * 10));
        paper.setTotalTime(5);
        paper.setPassScore(BigDecimal.valueOf(selected.size() * 6));
        paper.setType("manual");
        paper.setStatus("active");
        examPaperMapper.insert(paper);

        BigDecimal perScore = BigDecimal.TEN;
        for (int i = 0; i < selected.size(); i++) {
            ExamPaperQuestion epq = new ExamPaperQuestion();
            epq.setPaperId(paper.getId());
            epq.setQuestionId(selected.get(i).getId());
            epq.setQuestionOrder(i + 1);
            epq.setScore(perScore);
            examPaperQuestionMapper.insert(epq);
        }

        LocalDateTime now = LocalDateTime.now();

        Exam exam = new Exam();
        exam.setName("[挑战] 限时对决");
        exam.setPaperId(paper.getId());
        exam.setDescription("挑战模式");
        exam.setExamType("permanent");
        exam.setStartTime(now);
        exam.setEndTime(now.plusYears(1));
        exam.setDuration(5); // 挑战模式5分钟
        exam.setAllowLate(true);
        exam.setCreatedBy(userId);
        exam.setStatus("published");
        exam.setAutoPublish(false);
        exam.setEnableRecording(false);
        examMapper.insert(exam);

        ExamRecord record = new ExamRecord();
        record.setExamId(exam.getId());
        record.setPaperId(paper.getId());
        record.setStudentId(userId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("ongoing");
        record.setAutoScore(BigDecimal.ZERO);
        record.setManualScore(BigDecimal.ZERO);
        record.setTotalScore(BigDecimal.ZERO);
        examRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("examId", exam.getId());
        result.put("paperId", paper.getId());
        result.put("questionCount", selected.size());
        result.put("challengeMode", true);
        return Result.success(result);
    }
}
