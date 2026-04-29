/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80013
 Source Host           : localhost:3306
 Source Schema         : Beiming-OnlineExam

 Target Server Type    : MySQL
 Target Server Version : 80013
 File Encoding         : 65001

 Date: 16/07/2024 16:00:11
*/

SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS `Beiming-OnlineExam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `Beiming-OnlineExam`;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` varchar(50) DEFAULT 'active' COMMENT '记录状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin', '管理员', 'http://localhost:9090/files/download/1721114905635-柴犬.jpeg', 'ADMIN', '13800000001', 'admin@example.com', NOW(), NOW(), 'active');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '系统维护通知', '系统将在2026年3月10日凌晨2点进行维护，请各位玩家提前完成审核，避免影响正常入服流程。', '2025-03-09 18:00:00', NOW());
INSERT INTO `notice` VALUES (2, '入服审核安排公告', '本次入服审核将于2026年3月15日开始，请各位玩家在规定时间内完成审核。审核内容包括：建筑审核、后期审核、红石审核、普通(见习)审核。', '2026-03-10 10:00:00', NOW());
INSERT INTO `notice` VALUES (3, '作弊防范提示', '为了确保考试的公正性，本系统启用了实时监控功能，考试过程中请勿切换窗口或使用其他应用，违者将被警告并自动终止考试。', '2026-03-10 10:30:00', NOW());

-- ----------------------------
-- Table structure for action_logs (审计日志)
-- ----------------------------
DROP TABLE IF EXISTS `action_logs`;
CREATE TABLE `action_logs`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` int(10) NOT NULL COMMENT '管理员ID',
  `action_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `action_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作详情',
  `action_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for examiner (阅卷人表)
-- ----------------------------
DROP TABLE IF EXISTS `examiner`;
CREATE TABLE `examiner`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` varchar(50) DEFAULT 'active' COMMENT '记录状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '阅卷人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of examiner
-- ----------------------------
INSERT INTO `examiner` VALUES (1, 'examiner', 'examiner', '阅卷人', 'http://localhost:9090/files/download/1721114905635-柴犬.jpeg', 'HELPER', '13800000002', 'examiner@example.com', NOW(), NOW(), 'active');

-- ----------------------------
-- Table structure for student (玩家表，保留历史表名)
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_no` varchar(50) NULL COMMENT '玩家编号',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `class_name` varchar(100) NULL COMMENT '分组',
  `branch` varchar(50) NULL COMMENT '分支',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` varchar(50) DEFAULT 'PENDING' COMMENT '账号状态: APPROVED-审核通过, PENDING-审核中, REJECTED-审核未通过',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_no` (`student_no`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '玩家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, 'BM0001', 'student', 'student', '示例玩家', 'http://localhost:9090/files/download/1721114905635-柴犬.jpeg', 'USER', '13800000003', 'player@example.com', '普通(见习)审核', '普通(见习)审核', NOW(), NOW(), 'active');

-- ===========================================
-- 北冥审核系统核心表
-- ===========================================

-- ----------------------------
-- Table structure for question_category (题目分类表)
-- ----------------------------
DROP TABLE IF EXISTS `question_category`;
CREATE TABLE `question_category`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` varchar(500) NULL COMMENT '分类描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT 'active',
  `parent_id` int(10) NULL COMMENT '父分类ID，null表示顶级学科',
  `icon` varchar(100) NULL COMMENT '图标标识',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB COMMENT = '题目分类表';

-- ----------------------------
-- Records of question_category
-- ----------------------------
INSERT INTO `question_category` (`id`, `name`, `description`, `created_at`, `updated_at`, `status`, `parent_id`, `icon`) VALUES
(1, 'Minecraft Java 生电服务器进服审核', '面向生电服务器成员准入的四项审核题库', NOW(), NOW(), 'active', NULL, 'minecraft'),
(2, '建筑审核', '建筑审美、结构表达、风格统一与工程协作审核', NOW(), NOW(), 'active', 1, NULL),
(3, '后期审核', '物资统计、仓储整理、工程收尾与文档记录审核', NOW(), NOW(), 'active', 1, NULL),
(4, '红石审核', '红石机器原理、性能影响、区块加载与维护能力审核', NOW(), NOW(), 'active', 1, NULL),
(5, '普通(见习)审核', '服务器规则、基础生存、生电常识与协作意识审核', NOW(), NOW(), 'active', 1, NULL);

-- ----------------------------
-- Table structure for question (题目表)
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `category_id` int(10) NULL,
  `type` varchar(20) NOT NULL COMMENT 'single/multiple/judge/fillin/essay',
  `content` text NOT NULL COMMENT '题目内容',
  `options` json NULL COMMENT '选项JSON',
  `answer` varchar(500) NOT NULL COMMENT '答案',
  `analysis` text NULL COMMENT '答案解析',
  `images` json NULL COMMENT '题目图片JSON: [{"url":"...", "caption":"..."}]',
  `difficulty` varchar(20) DEFAULT 'medium' COMMENT 'easy/medium/hard',
  `score` decimal(5,2) DEFAULT NULL,
  `created_by` int(10) NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT 'active',
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_type` (`type`)
) ENGINE = InnoDB COMMENT = '题目表';

-- ----------------------------
-- Records of question (示例数据)
-- ----------------------------
INSERT INTO `question` (`id`, `category_id`, `type`, `content`, `options`, `answer`, `analysis`, `images`, `difficulty`, `score`, `created_by`, `created_at`, `updated_at`, `status`) VALUES
(1, 2, 'essay', '请说明你会如何让公共建筑同时满足美观、可维护和服务器风格统一。', NULL, '要点：明确风格参考、控制体量比例、选材统一、预留维护空间、避免遮挡公共线路。', '建筑审核重点看审美表达、功能性、可维护性和团队协作意识。', NULL, 'hard', 10.0, 1, NOW(), NOW(), 'active'),
(2, 2, 'single', '公共建筑选址时，最应该优先避开什么？', '{"A": "主交通线、公共机器和预留工程区", "B": "所有平原地形", "C": "玩家个人审美", "D": "低亮度装饰"}', 'A', '生电服务器公共区域需要优先保证线路、机器、交通和后续扩建空间。', NULL, 'medium', 3.0, 1, NOW(), NOW(), 'active'),
(3, 3, 'essay', '请说明你能承担哪些后期工作，以及如何记录工程进度和物资消耗。', NULL, '要点：物资统计、仓储整理、施工收尾、文档记录、截图归档、异常反馈。', '后期审核重点看稳定执行、记录习惯和收尾能力。', NULL, 'medium', 8.0, 1, NOW(), NOW(), 'active'),
(4, 3, 'multiple', '大型工程收尾时，哪些事项必须检查？', '{"A": "临时方块是否清理", "B": "剩余物资是否归仓", "C": "说明牌和文档是否更新", "D": "是否刷屏庆祝"}', 'ABC', '临时结构、物资和文档是工程交付关键项。', NULL, 'medium', 5.0, 1, NOW(), NOW(), 'active'),
(5, 4, 'multiple', '设计大型红石机器前，需要提前说明哪些内容？', '{"A": "用途与产能", "B": "卡服风险", "C": "区块加载方式", "D": "机器外观颜色"}', 'ABC', '红石审核重点关注功能、性能、加载方式和维护成本。', NULL, 'medium', 5.0, 1, NOW(), NOW(), 'active'),
(6, 4, 'single', '红石机器导致 MSPT 明显升高时，正确处理方式是什么？', '{"A": "立刻停机并反馈排查", "B": "继续运行观察几天", "C": "隐藏机器避免被发现", "D": "增加更多漏斗"}', 'A', '性能异常要先停机，避免影响服务器整体稳定。', NULL, 'easy', 3.0, 1, NOW(), NOW(), 'active'),
(7, 5, 'single', '普通(见习)审核中，进入生电服务器前最应该确认哪项？', '{"A": "服务器版本、规则和准入要求", "B": "材质包颜色", "C": "个人皮肤稀有度", "D": "聊天昵称长度"}', 'A', '见习成员先确认版本、规则、权限边界和基础要求。', NULL, 'easy', 2.0, 1, NOW(), NOW(), 'active'),
(8, 5, 'judge', '未经沟通，不应擅自改动公共机器、公共仓储或他人工程。', NULL, 'true', '公共设施变更需要沟通和记录，避免破坏服务器协作秩序。', NULL, 'easy', 2.0, 1, NOW(), NOW(), 'active');

-- ----------------------------
-- Table structure for exam_paper (试卷表)
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '试卷名称',
  `description` text NULL,
  `total_score` decimal(5,2) NOT NULL DEFAULT 100.00,
  `total_time` int(10) NOT NULL COMMENT '考试时长(分钟)',
  `pass_score` decimal(5,2) DEFAULT 60.00,
  `type` varchar(20) DEFAULT 'manual' COMMENT 'manual/random',
  `random_config` json NULL,
  `created_by` int(10) NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT 'active',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '试卷表';

-- ----------------------------
-- Table structure for exam_paper_question (试卷题目关联表)
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper_question`;
CREATE TABLE `exam_paper_question`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `paper_id` int(10) NOT NULL,
  `question_id` int(10) NOT NULL,
  `question_order` int(10) NOT NULL COMMENT '题目序号',
  `score` decimal(5,2) NOT NULL COMMENT '本题分值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_order`),
  INDEX `idx_paper` (`paper_id`),
  INDEX `idx_question` (`question_id`)
) ENGINE = InnoDB COMMENT = '试卷题目关联表';

-- ----------------------------
-- Table structure for exam (考试表)
-- ----------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `paper_id` int(10) NOT NULL,
  `description` text NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `duration` int(10) NOT NULL COMMENT '考试时长(分钟)',
  `allow_late` tinyint(1) DEFAULT 0,
  `late_time` int(10) NULL,
  `created_by` int(10) NOT NULL,
  `branch` varchar(50) NULL COMMENT '分支',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(50) DEFAULT 'draft',
  PRIMARY KEY (`id`),
  INDEX `idx_paper` (`paper_id`),
  INDEX `idx_time` (`start_time`, `end_time`)
) ENGINE = InnoDB COMMENT = '考试表';

-- ----------------------------
-- Table structure for exam_permission (考试权限表)
-- ----------------------------
DROP TABLE IF EXISTS `exam_permission`;
CREATE TABLE `exam_permission`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `exam_id` int(10) NOT NULL,
  `student_id` int(10) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_student` (`exam_id`, `student_id`),
  INDEX `idx_exam` (`exam_id`),
  INDEX `idx_student` (`student_id`)
) ENGINE = InnoDB COMMENT = '考试权限表';

-- ----------------------------
-- Table structure for exam_record (考试记录表)
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `exam_id` int(10) NOT NULL,
  `paper_id` int(10) NOT NULL,
  `student_id` int(10) NOT NULL,
  `start_time` datetime NOT NULL,
  `submit_time` datetime NULL,
  `duration` int(10) NULL COMMENT '实际用时(秒)',
  `status` varchar(50) DEFAULT 'ongoing',
  `auto_score` decimal(5,2) DEFAULT 0.00,
  `manual_score` decimal(5,2) DEFAULT 0.00,
  `total_score` decimal(5,2) DEFAULT 0.00,
  `is_pass` tinyint(1) NULL,
  `exam_status` varchar(50) DEFAULT 'PENDING' COMMENT '考核状态: PENDING-待审阅, UNDER_REVIEW-审阅中, PASSED-考核通过, FAILED-未通过考核',
  `approved_by` int(10) NULL COMMENT '审批人ID',
  `approved_at` datetime NULL COMMENT '审批时间',
  `is_published` tinyint(1) DEFAULT 0 COMMENT '是否公示',
  `published_at` datetime NULL COMMENT '公示时间',
  `switch_count` int(10) DEFAULT 0 COMMENT '切屏次数',
  `chief_examiner_id` int(10) NULL COMMENT '主考官ID',
  `chief_examiner_role` varchar(20) NULL COMMENT '主考官角色',
  `chief_examiner_name` varchar(50) NULL COMMENT '主考官姓名',
  `final_decision_reason` varchar(500) NULL COMMENT '最终判定理由',
  `final_evaluated_at` datetime NULL COMMENT '最终判定时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_exam` (`exam_id`),
  INDEX `idx_student` (`student_id`),
  INDEX `idx_status` (`status`)
) ENGINE = InnoDB COMMENT = '考试记录表';

-- ----------------------------
-- Table structure for exam_answer (答题记录表)
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `record_id` int(10) NOT NULL,
  `question_id` int(10) NOT NULL,
  `student_answer` text NULL,
  `is_correct` tinyint(1) NULL,
  `score` decimal(5,2) DEFAULT 0.00,
  `comment` text NULL,
  `graded_by` int(10) NULL,
  `graded_at` datetime NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_question` (`record_id`, `question_id`),
  INDEX `idx_record` (`record_id`),
  INDEX `idx_question` (`question_id`)
) ENGINE = InnoDB COMMENT = '答题记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for achievement_record (成就记录表)
-- ----------------------------
DROP TABLE IF EXISTS `achievement_record`;
CREATE TABLE `achievement_record` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(10) NOT NULL COMMENT '用户ID',
  `user_name` varchar(255) DEFAULT NULL COMMENT '用户名',
  `user_role` varchar(50) DEFAULT NULL COMMENT '用户角色',
  `achievement_key` varchar(100) NOT NULL COMMENT '成就唯一标识',
  `achievement_name` varchar(255) DEFAULT NULL COMMENT '成就名称',
  `description` varchar(500) DEFAULT NULL COMMENT '成就描述',
  `icon` varchar(50) DEFAULT NULL COMMENT '成就图标',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `score` int(10) DEFAULT 0 COMMENT '奖励积分',
  `earned_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  `notified` tinyint(1) DEFAULT 0 COMMENT '是否已通知',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_achievement` (`user_id`, `user_role`, `achievement_key`),
  INDEX `idx_user` (`user_id`, `user_role`),
  INDEX `idx_key` (`achievement_key`)
) ENGINE = InnoDB COMMENT = '成就记录表';

-- ----------------------------
-- Table structure for exam_record_compare (成绩对比表，存储历史统计数据)
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_compare`;
CREATE TABLE `exam_record_compare` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `exam_id` int(10) NOT NULL COMMENT '考试ID',
  `avg_score` decimal(5,2) DEFAULT 0 COMMENT '平均分',
  `max_score` decimal(5,2) DEFAULT 0 COMMENT '最高分',
  `min_score` decimal(5,2) DEFAULT 0 COMMENT '最低分',
  `total_count` int(10) DEFAULT 0 COMMENT '参考人数',
  `pass_count` int(10) DEFAULT 0 COMMENT '通过人数',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam` (`exam_id`)
) ENGINE = InnoDB COMMENT = '成绩统计表';
