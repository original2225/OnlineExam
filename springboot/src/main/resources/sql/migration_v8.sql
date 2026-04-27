-- Migration V8: 成就系统 & 考试成绩统计
-- 执行前请确认数据库连接正常

-- 1. achievement_record 成就记录表
CREATE TABLE IF NOT EXISTS `achievement_record` (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `user_name` varchar(50) DEFAULT NULL COMMENT '用户姓名',
    `user_role` varchar(20) NOT NULL COMMENT '用户角色',
    `achievement_key` varchar(50) NOT NULL COMMENT '成就唯一标识',
    `achievement_name` varchar(100) NOT NULL COMMENT '成就名称',
    `description` varchar(500) DEFAULT NULL COMMENT '成就描述',
    `icon` varchar(50) DEFAULT NULL COMMENT '成就图标/emoji',
    `category` varchar(30) DEFAULT NULL COMMENT '分类: exam/practice/checkin/social',
    `score` int DEFAULT 0 COMMENT '奖励积分',
    `earned_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    `notified` tinyint(1) DEFAULT 0 COMMENT '是否已发送通知',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `user_role`, `achievement_key`),
    KEY `idx_user` (`user_id`, `user_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就记录表';

-- 2. exam_record_compare 考试成绩统计表
CREATE TABLE IF NOT EXISTS `exam_record_compare` (
    `id` int NOT NULL AUTO_INCREMENT,
    `exam_id` int NOT NULL COMMENT '考试ID',
    `avg_score` decimal(10,2) DEFAULT NULL COMMENT '平均分',
    `max_score` decimal(10,2) DEFAULT NULL COMMENT '最高分',
    `min_score` decimal(10,2) DEFAULT NULL COMMENT '最低分',
    `total_count` int DEFAULT 0 COMMENT '参考人数',
    `pass_count` int DEFAULT 0 COMMENT '及格人数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试成绩统计表';

-- 3. exam_record 新增 switch_count 字段（如果 enhancement_features.sql 已执行可跳过）
-- ALTER TABLE `exam_record` ADD COLUMN `switch_count` int DEFAULT 0;
