-- Minecraft Java 生电服务器进服审核示例题库
-- 分类：建筑审核、后期审核、红石审核、普通(见习)审核

INSERT INTO `question_category` (`name`, `description`, `status`, `icon`)
SELECT 'Minecraft Java 生电服务器进服审核', '面向生电服务器成员准入的四项审核题库', 'active', 'minecraft'
WHERE NOT EXISTS (SELECT 1 FROM `question_category` WHERE `name` = 'Minecraft Java 生电服务器进服审核');

SET @audit_parent_id = (SELECT id FROM `question_category` WHERE `name` = 'Minecraft Java 生电服务器进服审核' LIMIT 1);

INSERT INTO `question_category` (`name`, `description`, `parent_id`, `status`)
SELECT '建筑审核', '建筑审美、结构表达、风格统一与工程协作审核', @audit_parent_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question_category` WHERE `name` = '建筑审核' AND `parent_id` = @audit_parent_id);
INSERT INTO `question_category` (`name`, `description`, `parent_id`, `status`)
SELECT '后期审核', '物资统计、仓储整理、工程收尾与文档记录审核', @audit_parent_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question_category` WHERE `name` = '后期审核' AND `parent_id` = @audit_parent_id);
INSERT INTO `question_category` (`name`, `description`, `parent_id`, `status`)
SELECT '红石审核', '红石机器原理、性能影响、区块加载与维护能力审核', @audit_parent_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question_category` WHERE `name` = '红石审核' AND `parent_id` = @audit_parent_id);
INSERT INTO `question_category` (`name`, `description`, `parent_id`, `status`)
SELECT '普通(见习)审核', '服务器规则、基础生存、生电常识与协作意识审核', @audit_parent_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question_category` WHERE `name` = '普通(见习)审核' AND `parent_id` = @audit_parent_id);

SET @building_id = (SELECT id FROM `question_category` WHERE `name` = '建筑审核' AND `parent_id` = @audit_parent_id LIMIT 1);
SET @post_id = (SELECT id FROM `question_category` WHERE `name` = '后期审核' AND `parent_id` = @audit_parent_id LIMIT 1);
SET @redstone_id = (SELECT id FROM `question_category` WHERE `name` = '红石审核' AND `parent_id` = @audit_parent_id LIMIT 1);
SET @trainee_id = (SELECT id FROM `question_category` WHERE `name` = '普通(见习)审核' AND `parent_id` = @audit_parent_id LIMIT 1);

INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'essay', '请说明你会如何让公共建筑同时满足美观、可维护和服务器风格统一。', NULL, '要点：明确风格参考、控制体量比例、选材统一、预留维护空间、避免遮挡公共线路。', '建筑审核重点看审美表达、功能性、可维护性和团队协作意识。', 'hard', 10, @building_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '请说明你会如何让公共建筑同时满足美观、可维护和服务器风格统一。');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'single', '公共建筑选址时，最应该优先避开什么？', '{"A":"主交通线、公共机器和预留工程区","B":"所有平原地形","C":"玩家个人审美","D":"低亮度装饰"}', 'A', '生电服务器公共区域需要优先保证线路、机器、交通和后续扩建空间。', 'medium', 3, @building_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '公共建筑选址时，最应该优先避开什么？');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'essay', '请说明你能承担哪些后期工作，以及如何记录工程进度和物资消耗。', NULL, '要点：物资统计、仓储整理、施工收尾、文档记录、截图归档、异常反馈。', '后期审核重点看稳定执行、记录习惯和收尾能力。', 'medium', 8, @post_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '请说明你能承担哪些后期工作，以及如何记录工程进度和物资消耗。');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'multiple', '大型工程收尾时，哪些事项必须检查？', '{"A":"临时方块是否清理","B":"剩余物资是否归仓","C":"说明牌和文档是否更新","D":"是否刷屏庆祝"}', 'ABC', '临时结构、物资和文档是工程交付关键项。', 'medium', 5, @post_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '大型工程收尾时，哪些事项必须检查？');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'multiple', '设计大型红石机器前，需要提前说明哪些内容？', '{"A":"用途与产能","B":"卡服风险","C":"区块加载方式","D":"机器外观颜色"}', 'ABC', '红石审核重点关注功能、性能、加载方式和维护成本。', 'medium', 5, @redstone_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '设计大型红石机器前，需要提前说明哪些内容？');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'single', '红石机器导致 MSPT 明显升高时，正确处理方式是什么？', '{"A":"立刻停机并反馈排查","B":"继续运行观察几天","C":"隐藏机器避免被发现","D":"增加更多漏斗"}', 'A', '性能异常要先停机，避免影响服务器整体稳定。', 'easy', 3, @redstone_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '红石机器导致 MSPT 明显升高时，正确处理方式是什么？');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'single', '普通(见习)审核中，进入生电服务器前最应该确认哪项？', '{"A":"服务器版本、规则和准入要求","B":"材质包颜色","C":"个人皮肤稀有度","D":"聊天昵称长度"}', 'A', '见习成员先确认版本、规则、权限边界和基础要求。', 'easy', 2, @trainee_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '普通(见习)审核中，进入生电服务器前最应该确认哪项？');
INSERT INTO `question` (`type`, `content`, `options`, `answer`, `analysis`, `difficulty`, `score`, `category_id`, `status`)
SELECT 'judge', '未经沟通，不应擅自改动公共机器、公共仓储或他人工程。', NULL, 'true', '公共设施变更需要沟通和记录，避免破坏服务器协作秩序。', 'easy', 2, @trainee_id, 'active'
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `content` = '未经沟通，不应擅自改动公共机器、公共仓储或他人工程。');
