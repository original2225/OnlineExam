-- 邀请码表
CREATE TABLE IF NOT EXISTS `invitation_code` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `code` VARCHAR(20) NOT NULL UNIQUE COMMENT '邀请码',
    `status` VARCHAR(20) NOT NULL DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未使用，USED-已使用，EXPIRED-已过期',
    `used_by` INT COMMENT '使用用户ID',
    `used_by_username` VARCHAR(50) COMMENT '使用用户名',
    `used_time` DATETIME COMMENT '使用时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` INT COMMENT '创建者ID（管理员）',
    `expire_time` DATETIME COMMENT '过期时间',
    `remark` VARCHAR(200) COMMENT '备注',
    `target_role` VARCHAR(20) DEFAULT 'USER' COMMENT '目标角色: USER-成员, HELPER-审核员',
    INDEX idx_code (`code`),
    INDEX idx_status (`status`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码表';
