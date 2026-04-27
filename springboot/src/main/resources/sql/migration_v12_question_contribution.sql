-- 题目投稿审核入库关联字段
SET @schema_name = DATABASE();

SELECT COUNT(*) INTO @missing_approved_question_id
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'question_contribution'
  AND COLUMN_NAME = 'approved_question_id';

SET @sql = IF(@missing_approved_question_id = 0,
  'ALTER TABLE `question_contribution` ADD COLUMN `approved_question_id` int NULL COMMENT ''审核通过后入库题目ID'' AFTER `reviewed_at`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
