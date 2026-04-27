package com.example.service;

import com.example.entity.AchievementRecord;
import com.example.mapper.AchievementRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AchievementService {

    private static final Map<String, AchievementDef> DEFINITIONS = new LinkedHashMap<>();
    static {
        // 考试类
        DEFINITIONS.put("first_exam", new AchievementDef("first_exam", "初出茅庐", "完成人生第一场考试", "🎯", "exam", 5));
        DEFINITIONS.put("perfect_score", new AchievementDef("perfect_score", "满分答卷", "考试获得满分", "💯", "exam", 20));
        DEFINITIONS.put("exam_master", new AchievementDef("exam_master", "考试大师", "累计完成10场考试", "🏆", "exam", 15));
        DEFINITIONS.put("streak_pass", new AchievementDef("streak_pass", "连战连捷", "连续5场考试通过", "🔥", "exam", 25));

        // 练习类
        DEFINITIONS.put("first_practice", new AchievementDef("first_practice", "初试牛刀", "完成第一次练习", "✏️", "practice", 3));
        DEFINITIONS.put("question_collector", new AchievementDef("question_collector", "刷题达人", "累计完成100道练习题", "📚", "practice", 20));
        DEFINITIONS.put("speed_demon", new AchievementDef("speed_demon", "闪电侠", "单次练习正确率100%且用时最短", "⚡", "practice", 15));
        DEFINITIONS.put("no_wrong", new AchievementDef("no_wrong", "一题不漏", "单次练习连续答对20题", "🎯", "practice", 10));

        // 打卡类
        DEFINITIONS.put("first_checkin", new AchievementDef("first_checkin", "每日打卡", "完成第一次学习打卡", "📅", "checkin", 3));
        DEFINITIONS.put("week_warrior", new AchievementDef("week_warrior", "周冠军", "连续打卡7天", "🏅", "checkin", 15));
        DEFINITIONS.put("month_legend", new AchievementDef("month_legend", "月传说", "连续打卡30天", "👑", "checkin", 50));
        DEFINITIONS.put("century_student", new AchievementDef("century_student", "百年学子", "连续打卡100天", "📜", "checkin", 100));

        // 社交类
        DEFINITIONS.put("contributor", new AchievementDef("contributor", "出题官", "成功提交1道题目被采纳", "📝", "social", 15));
        DEFINITIONS.put("mentor", new AchievementDef("mentor", "答疑达人", "在满分批注中获得10个赞", "💬", "social", 20));
        DEFINITIONS.put("social_star", new AchievementDef("social_star", "社交之星", "在论坛发布5篇帖子", "🌟", "social", 10));

        // 探索类
        DEFINITIONS.put("egg_hunter", new AchievementDef("egg_hunter", "彩蛋猎人", "发现3个隐藏彩蛋", "🥚", "social", 15));
        DEFINITIONS.put("all_achieved", new AchievementDef("all_achieved", "全能学霸", "解锁全部成就", "🌈", "exam", 100));
    }

    private final AchievementRecordMapper mapper;

    public AchievementService(AchievementRecordMapper mapper) {
        this.mapper = mapper;
    }

    public record AchievementDef(String key, String name, String desc, String icon, String category, int score) {}

    /**
     * 尝试解锁成就
     * @return 解锁的成就，如果没有新成就返回null
     */
    public AchievementRecord tryUnlock(Integer userId, String userName, String userRole, String achievementKey) {
        // 检查是否已有此成就
        AchievementRecord existing = mapper.selectByUserAndKey(userId, userRole, achievementKey);
        if (existing != null) return null;

        AchievementDef def = DEFINITIONS.get(achievementKey);
        if (def == null) return null;

        AchievementRecord record = new AchievementRecord();
        record.setUserId(userId);
        record.setUserName(userName);
        record.setUserRole(userRole);
        record.setAchievementKey(achievementKey);
        record.setAchievementName(def.name());
        record.setDescription(def.desc());
        record.setIcon(def.icon());
        record.setCategory(def.category());
        record.setScore(def.score());
        record.setEarnedAt(LocalDateTime.now());
        record.setNotified(false);

        mapper.insert(record);
        return record;
    }

    /**
     * 获取用户所有成就
     */
    public List<AchievementRecord> getUserAchievements(Integer userId, String userRole) {
        return mapper.selectByUser(userId, userRole);
    }

    /**
     * 获取所有已解锁的成就
     */
    public List<AchievementRecord> getAllUnlocked() {
        return mapper.selectAll();
    }

    /**
     * 获取所有成就定义（包括未解锁的）
     */
    public List<Map<String, Object>> getAllAchievements(Integer userId, String userRole) {
        List<AchievementRecord> unlocked = mapper.selectByUser(userId, userRole);
        Set<String> unlockedKeys = new HashSet<>();
        unlocked.forEach(r -> unlockedKeys.add(r.getAchievementKey()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, AchievementDef> entry : DEFINITIONS.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", entry.getKey());
            item.put("name", entry.getValue().name());
            item.put("description", entry.getValue().desc());
            item.put("icon", entry.getValue().icon());
            item.put("category", entry.getValue().category());
            item.put("score", entry.getValue().score());
            item.put("unlocked", unlockedKeys.contains(entry.getKey()));
            if (unlockedKeys.contains(entry.getKey())) {
                AchievementRecord rec = unlocked.stream().filter(r -> r.getAchievementKey().equals(entry.getKey())).findFirst().orElse(null);
                item.put("earnedAt", rec != null ? rec.getEarnedAt() : null);
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 获取成就统计
     */
    public Map<String, Object> getStats(Integer userId, String userRole) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", DEFINITIONS.size());
        stats.put("unlocked", mapper.countByUser(userId, userRole));
        stats.put("totalScore", getUserAchievements(userId, userRole).stream().mapToInt(AchievementRecord::getScore).sum());
        return stats;
    }
}
