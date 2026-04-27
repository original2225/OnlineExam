-- 注册申请表
CREATE TABLE IF NOT EXISTS `registration_request` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) COMMENT '姓名',
    `student_no` VARCHAR(20) COMMENT '学号',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `class_name` VARCHAR(50) COMMENT '分组',
    `branch` VARCHAR(50) COMMENT '分支',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝',
    `request_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approval_time` DATETIME COMMENT '审批时间',
    `approved_by` INT COMMENT '审批人ID',
    `rejection_reason` VARCHAR(200) COMMENT '拒绝原因',
    `approval_token` VARCHAR(100) UNIQUE COMMENT '审批Token',
    INDEX idx_status (`status`),
    INDEX idx_token (`approval_token`),
    INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注册申请表';
