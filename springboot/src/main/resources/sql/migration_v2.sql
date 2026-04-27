-- 数据库迁移脚本：添加权限等级和题目增强
-- 执行日期: 2026-04-02

-- 1. admin 表添加 level 字段（权限等级）
ALTER TABLE `admin` ADD COLUMN `level` INT DEFAULT 3 COMMENT '权限等级: 3-Helper, 4-Owner';

-- 将默认管理员设为 Owner (level=4)，其他管理员为 Helper (level=3)
-- huachan 用户（如果存在）设为 Owner
UPDATE `admin` SET `level` = 4 WHERE `username` = 'huachan';
UPDATE `admin` SET `level` = 4 WHERE `id` = 1;

-- 2. question 表添加 images 字段
ALTER TABLE `question` ADD COLUMN `images` JSON NULL COMMENT '题目图片JSON: [{"url":"...", "caption":"..."}]';

-- 3. invitation_code 表添加 target_role 字段
ALTER TABLE `invitation_code` ADD COLUMN `target_role` VARCHAR(20) DEFAULT 'STUDENT' COMMENT '目标角色: STUDENT-考生, EXAMINER-阅卷人';

-- 4. 更新已有的邀请码默认为考生类型
UPDATE `invitation_code` SET `target_role` = 'STUDENT' WHERE `target_role` IS NULL;
