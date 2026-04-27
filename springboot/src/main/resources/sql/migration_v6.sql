-- =====================================================
-- 交流论坛模块 - migration_v6.sql
-- =====================================================

-- 帖子表
CREATE TABLE IF NOT EXISTS `forum_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容(HTML)',
  `author_id` int NOT NULL COMMENT '作者ID',
  `author_name` varchar(100) NULL COMMENT '作者姓名',
  `author_role` varchar(50) NULL COMMENT '作者角色',
  `author_avatar` varchar(500) NULL COMMENT '作者头像',
  `category` varchar(50) NULL COMMENT '标签分类',
  `tags` varchar(500) NULL COMMENT '标签(逗号分隔)',
  `cover_url` varchar(500) NULL COMMENT '封面图URL',
  `view_count` int DEFAULT 0 COMMENT '浏览量',
  `like_count` int DEFAULT 0 COMMENT '点赞数',
  `comment_count` int DEFAULT 0 COMMENT '评论数',
  `is_top` tinyint(1) DEFAULT 0 COMMENT '是否置顶 0否1是',
  `is_locked` tinyint(1) DEFAULT 0 COMMENT '是否锁定 0否1是',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/hidden/deleted',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_author_id` (`author_id`),
  INDEX `idx_category` (`category`),
  INDEX `idx_is_top` (`is_top`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子表';

-- 评论表
CREATE TABLE IF NOT EXISTS `forum_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL COMMENT '帖子ID',
  `content` text NOT NULL COMMENT '评论内容',
  `author_id` int NOT NULL COMMENT '作者ID',
  `author_name` varchar(100) NULL COMMENT '作者姓名',
  `author_role` varchar(50) NULL COMMENT '作者角色',
  `author_avatar` varchar(500) NULL COMMENT '作者头像',
  `parent_id` int NULL COMMENT '父评论ID(null=顶级评论)',
  `like_count` int DEFAULT 0 COMMENT '点赞数',
  `status` varchar(20) DEFAULT 'active' COMMENT 'active/deleted',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_post_id` (`post_id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论表';

-- 点赞/收藏记录表
CREATE TABLE IF NOT EXISTS `forum_like` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `user_role` varchar(50) NOT NULL COMMENT '用户角色',
  `target_type` varchar(20) NOT NULL COMMENT 'post/comment',
  `target_id` int NOT NULL COMMENT '目标ID',
  `type` varchar(20) NOT NULL COMMENT 'like/favorite',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `user_role`, `target_type`, `target_id`, `type`),
  INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛点赞收藏记录表';

-- 如果帖子表已存在但缺少 tags/cover_url 列，执行以下语句（如已存在会报错可忽略）
-- ALTER TABLE `forum_post` ADD COLUMN `tags` varchar(500) NULL COMMENT '标签(逗号分隔)' AFTER `category`;
-- ALTER TABLE `forum_post` ADD COLUMN `cover_url` varchar(500) NULL COMMENT '封面图URL' AFTER `tags`;
