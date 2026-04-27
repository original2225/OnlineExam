SET @schema_name = DATABASE();

SELECT COUNT(*) INTO @missing_egg_name
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'easter_egg_record'
  AND COLUMN_NAME = 'egg_name';
SET @sql = IF(@missing_egg_name = 0,
  'ALTER TABLE `easter_egg_record` ADD COLUMN `egg_name` varchar(100) NOT NULL DEFAULT ''logo_click'' COMMENT ''彩蛋名称''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @old_user_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'easter_egg_record'
  AND INDEX_NAME = 'uk_user';
SET @sql = IF(@old_user_index > 0,
  'ALTER TABLE `easter_egg_record` DROP INDEX `uk_user`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @new_user_egg_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'easter_egg_record'
  AND INDEX_NAME = 'uk_user_egg';
SET @sql = IF(@new_user_egg_index = 0,
  'ALTER TABLE `easter_egg_record` ADD UNIQUE KEY `uk_user_egg` (`user_id`, `user_role`, `egg_name`)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @egg_time_index
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'easter_egg_record'
  AND INDEX_NAME = 'idx_egg_time';
SET @sql = IF(@egg_time_index = 0,
  'ALTER TABLE `easter_egg_record` ADD KEY `idx_egg_time` (`egg_name`, `discover_time`)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
