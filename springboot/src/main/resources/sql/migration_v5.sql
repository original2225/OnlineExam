-- 例题讲解教程表
CREATE TABLE IF NOT EXISTS `tutorial` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '标题',
  `description` varchar(500) NULL COMMENT '简介',
  `content_type` varchar(20) NOT NULL DEFAULT 'video' COMMENT '类型: video/image_text',
  `cover_url` varchar(500) NULL COMMENT '封面图URL',
  `video_url` varchar(500) NULL COMMENT '视频URL',
  `content` text NULL COMMENT '图文内容(HTML)',
  `category_id` int NULL COMMENT '关联分类ID',
  `creator_id` int NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(100) NULL COMMENT '创建者姓名',
  `creator_role` varchar(50) NULL COMMENT '创建者角色',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active/draft',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='例题讲解教程表';
