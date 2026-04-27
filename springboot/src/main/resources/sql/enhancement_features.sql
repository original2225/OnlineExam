-- 题目收藏表
CREATE TABLE IF NOT EXISTS `question_favorite` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_role` varchar(20) NOT NULL,
  `question_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_question` (`user_id`, `user_role`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目收藏表';

-- 审核打卡记录表
CREATE TABLE IF NOT EXISTS `study_checkin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_role` varchar(20) NOT NULL,
  `checkin_date` date NOT NULL,
  `streak_days` int DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `user_role`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核打卡记录表';

-- 用户贡献题目表
CREATE TABLE IF NOT EXISTS `question_contribution` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_name` varchar(50),
  `user_role` varchar(20),
  `category_id` int,
  `type` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `options` text,
  `answer` varchar(500) NOT NULL,
  `analysis` text,
  `difficulty` varchar(20) DEFAULT 'medium',
  `status` varchar(20) DEFAULT 'PENDING',
  `reviewer_id` int,
  `reviewer_name` varchar(50),
  `review_comment` text,
  `reviewed_at` datetime,
  `approved_question_id` int,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户贡献题目表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `user_role` varchar(20) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text,
  `type` varchar(30) DEFAULT 'system',
  `is_read` tinyint DEFAULT 0,
  `link` varchar(500),
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `user_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 扩展字段
ALTER TABLE `unified_user` ADD COLUMN `theme_preference` varchar(50) DEFAULT 'default';
ALTER TABLE `unified_user` ADD COLUMN `badge_list` varchar(500) DEFAULT '';
ALTER TABLE `exam_record` ADD COLUMN `switch_count` int DEFAULT 0;
