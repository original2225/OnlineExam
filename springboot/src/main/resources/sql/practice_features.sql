-- 题目批注表（满分考生可写批注）
CREATE TABLE IF NOT EXISTS `question_annotation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL COMMENT '题目ID',
  `user_id` int NOT NULL COMMENT '批注者ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '批注者姓名',
  `user_role` varchar(20) DEFAULT NULL COMMENT '批注者角色',
  `content` text NOT NULL COMMENT '批注内容',
  `like_count` int DEFAULT 0 COMMENT '点赞数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目批注表';

-- 错题集表
CREATE TABLE IF NOT EXISTS `wrong_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `user_role` varchar(20) NOT NULL COMMENT '用户角色',
  `question_id` int NOT NULL COMMENT '题目ID',
  `source` varchar(20) DEFAULT 'manual' COMMENT '来源: exam/practice/manual',
  `source_id` int DEFAULT NULL COMMENT '来源记录ID',
  `wrong_answer` text COMMENT '用户的错误答案',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_question` (`user_id`, `user_role`, `question_id`),
  KEY `idx_user` (`user_id`, `user_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题集表';
