-- 彩蛋发现记录表
CREATE TABLE IF NOT EXISTS `easter_egg_record` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` int(10) NOT NULL COMMENT '用户ID',
  `user_name` varchar(255) NULL COMMENT '用户名',
  `user_role` varchar(50) NULL COMMENT '用户角色',
  `discover_time` datetime NULL COMMENT '发现时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`, `user_role`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
