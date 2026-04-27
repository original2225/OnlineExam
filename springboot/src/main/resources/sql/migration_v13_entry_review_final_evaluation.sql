-- 入服审核最终表决与主考官字段
SET @schema_name = DATABASE();

SELECT COUNT(*) INTO @missing_performance_score
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'grading_submission' AND COLUMN_NAME = 'performance_score';
SET @sql = IF(@missing_performance_score = 0,
  'ALTER TABLE `grading_submission` ADD COLUMN `performance_score` decimal(5,2) NULL COMMENT ''玩家表现评分'' AFTER `total_score`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_advisory_vote
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'grading_submission' AND COLUMN_NAME = 'advisory_vote';
SET @sql = IF(@missing_advisory_vote = 0,
  'ALTER TABLE `grading_submission` ADD COLUMN `advisory_vote` varchar(20) NULL COMMENT ''参考表决: PASS/FAIL/ABSTAIN'' AFTER `performance_score`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_rejection_reasons
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'grading_submission' AND COLUMN_NAME = 'rejection_reasons';
SET @sql = IF(@missing_rejection_reasons = 0,
  'ALTER TABLE `grading_submission` ADD COLUMN `rejection_reasons` varchar(500) NULL COMMENT ''不通过原因，多选逗号分隔'' AFTER `advisory_vote`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_custom_reason
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'grading_submission' AND COLUMN_NAME = 'custom_reason';
SET @sql = IF(@missing_custom_reason = 0,
  'ALTER TABLE `grading_submission` ADD COLUMN `custom_reason` varchar(500) NULL COMMENT ''自定义不通过原因'' AFTER `rejection_reasons`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_chief_examiner_id
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'chief_examiner_id';
SET @sql = IF(@missing_chief_examiner_id = 0,
  'ALTER TABLE `exam_record` ADD COLUMN `chief_examiner_id` int NULL COMMENT ''主考官ID'' AFTER `switch_count`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_chief_examiner_role
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'chief_examiner_role';
SET @sql = IF(@missing_chief_examiner_role = 0,
  'ALTER TABLE `exam_record` ADD COLUMN `chief_examiner_role` varchar(20) NULL COMMENT ''主考官角色'' AFTER `chief_examiner_id`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_chief_examiner_name
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'chief_examiner_name';
SET @sql = IF(@missing_chief_examiner_name = 0,
  'ALTER TABLE `exam_record` ADD COLUMN `chief_examiner_name` varchar(50) NULL COMMENT ''主考官姓名'' AFTER `chief_examiner_role`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_final_decision_reason
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'final_decision_reason';
SET @sql = IF(@missing_final_decision_reason = 0,
  'ALTER TABLE `exam_record` ADD COLUMN `final_decision_reason` varchar(500) NULL COMMENT ''最终判定理由'' AFTER `chief_examiner_name`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @missing_final_evaluated_at
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'exam_record' AND COLUMN_NAME = 'final_evaluated_at';
SET @sql = IF(@missing_final_evaluated_at = 0,
  'ALTER TABLE `exam_record` ADD COLUMN `final_evaluated_at` datetime NULL COMMENT ''最终判定时间'' AFTER `final_decision_reason`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
