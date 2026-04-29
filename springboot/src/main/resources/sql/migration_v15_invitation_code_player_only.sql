ALTER TABLE `invitation_code`
    ADD COLUMN IF NOT EXISTS `used_by_username` varchar(50) DEFAULT NULL COMMENT '使用用户名',
    ADD COLUMN IF NOT EXISTS `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    ADD COLUMN IF NOT EXISTS `target_role` varchar(20) DEFAULT 'USER' COMMENT '目标角色: USER-玩家';

UPDATE `invitation_code`
SET `target_role` = 'USER'
WHERE `target_role` IS NULL OR `target_role` <> 'USER';
