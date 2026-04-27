-- 投票系统
CREATE TABLE IF NOT EXISTS `poll` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '投票标题',
  `description` text COMMENT '投票描述',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图URL',
  `creator_id` int NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建者名称',
  `creator_role` varchar(20) DEFAULT NULL COMMENT '创建者角色',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active/ended/draft',
  `max_choices` int DEFAULT 1 COMMENT '最多可选数, 1=单选, >1=多选',
  `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名投票',
  `total_votes` int DEFAULT 0 COMMENT '参与投票总人数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票表';

CREATE TABLE IF NOT EXISTS `poll_option` (
  `id` int NOT NULL AUTO_INCREMENT,
  `poll_id` int NOT NULL COMMENT '投票ID',
  `content` varchar(500) NOT NULL COMMENT '选项内容',
  `color` varchar(20) DEFAULT NULL COMMENT '选项颜色',
  `vote_count` int DEFAULT 0 COMMENT '得票数',
  `order_num` int DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`),
  KEY `idx_poll_id` (`poll_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票选项表';

CREATE TABLE IF NOT EXISTS `poll_vote` (
  `id` int NOT NULL AUTO_INCREMENT,
  `poll_id` int NOT NULL COMMENT '投票ID',
  `option_id` int NOT NULL COMMENT '选项ID',
  `user_id` int NOT NULL COMMENT '投票用户ID',
  `user_role` varchar(20) DEFAULT NULL COMMENT '用户角色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_poll_user_option` (`poll_id`, `user_id`, `option_id`),
  KEY `idx_poll_user` (`poll_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';
