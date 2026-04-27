-- Migration V7: 阅卷系统 - 审批/公示/公告/录屏

-- 1. exam_announcement 审核公告表
CREATE TABLE IF NOT EXISTS `exam_announcement` (
    `id` int NOT NULL AUTO_INCREMENT,
    `title` varchar(200) NOT NULL COMMENT '公告标题',
    `content` text COMMENT '公告内容',
    `type` varchar(20) DEFAULT 'general' COMMENT '类型: general/exam/result/notice',
    `priority` int DEFAULT 0 COMMENT '优先级, 越大越靠前',
    `exam_id` int DEFAULT NULL COMMENT '关联审核ID',
    `author_id` int NOT NULL COMMENT '发布人ID',
    `author_name` varchar(50) DEFAULT NULL COMMENT '发布人姓名',
    `status` varchar(20) DEFAULT 'published' COMMENT '状态: draft/published/archived',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_exam_id` (`exam_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核公告';

-- 3. exam_record 确保 exam_status 存在（如已存在可跳过此句）
-- ALTER TABLE `exam_record` ADD COLUMN `exam_status` varchar(20) DEFAULT 'PENDING' COMMENT '考核状态: PENDING/UNDER_REVIEW/PASSED/FAILED';

-- 4. exam_record 新增审批/公示字段
ALTER TABLE `exam_record`
    ADD COLUMN `approved_by` int DEFAULT NULL COMMENT '审批人ID',
    ADD COLUMN `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
    ADD COLUMN `is_published` tinyint(1) DEFAULT 0 COMMENT '是否已公示',
    ADD COLUMN `published_at` datetime DEFAULT NULL COMMENT '公示时间';

-- 5. exam 新增自动公示/录屏字段
ALTER TABLE `exam`
    ADD COLUMN `auto_publish` tinyint(1) DEFAULT 0 COMMENT '是否自动公示审核结果' AFTER `exam_type`,
    ADD COLUMN `enable_recording` tinyint(1) DEFAULT 0 COMMENT '是否开启录屏' AFTER `auto_publish`;

-- 6. exam_recording 录屏记录表
CREATE TABLE IF NOT EXISTS `exam_recording` (
    `id` int NOT NULL AUTO_INCREMENT,
    `exam_id` int NOT NULL COMMENT '审核ID',
    `student_id` int NOT NULL COMMENT '玩家ID',
    `record_id` int DEFAULT NULL COMMENT '审核记录ID',
    `file_url` varchar(500) DEFAULT NULL COMMENT '云盘文件链接',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
    `duration` int DEFAULT NULL COMMENT '录屏时长(秒)',
    `switch_count` int DEFAULT 0 COMMENT '切屏次数',
    `status` varchar(20) DEFAULT 'recording' COMMENT '状态: recording/uploaded/failed',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_exam_student` (`exam_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核录屏记录';
