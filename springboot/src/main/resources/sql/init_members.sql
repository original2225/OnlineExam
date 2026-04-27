-- 花蝉服成员数据初始化
-- 部门/分组映射: 后勤、后勤见习、红石、红石见习、建筑、休闲、见习、pvp、修仙、红石/炮 等
-- 密码统一为 123456（用户登录后自行修改）

-- 先清理旧数据（可选）
-- DELETE FROM student WHERE username IN (...);

INSERT INTO student (username, password, name, student_no, class_name, branch, phone, email, role, status) VALUES
-- 后勤
('huachan', '123456', '花蝉', 'huachan', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('yiuyo_', '123456', '侑', 'yiuyo_', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('hjj', '123456', '小贺', 'hjj', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('MaJiangla', '123456', '老狗', 'MaJiangla', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('chengpp', '123456', '诚', 'chengpp', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('cxy0320', '123456', '锅包肉', 'cxy0320', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('He_Ke', '123456', '何凯', 'He_Ke', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('thomasshen', '123456', 'th', 'thomasshen', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('Bei_jiawang', '123456', '小贝', 'Bei_jiawang', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('x_chengzi', '123456', '橙子', 'x_chengzi', '后勤/建筑', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('eVannnn', '123456', '呗吧呗', 'eVannnn', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('bububusda', '123456', '讲良心', 'bububusda', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 后勤见习
('zzzzzzzz', '123456', '懒猫', 'zzzzzzzz', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('Kleva', '123456', 'Kleva', 'Kleva', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('CiJius', '123456', '辞旧', 'CiJius', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('xiaokui', '123456', '小葵', 'xiaokui', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('wxj6662', '123456', '芙芙', 'wxj6662', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('Plus_Zl', '123456', '真理桑', 'Plus_Zl', '后勤见习', '后勤见习', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 红石/炮
('Ammonia_awa', '123456', '阿莫妮娅', 'Ammonia_awa', '红石/炮', '红石', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 红石见习
('qdn0719', '123456', '缺德牛', 'qdn0719', '红石见习', '红石见习', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 红石
('momokong', '123456', '沫沫', 'momokong', '红石', '红石', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 建筑
('blue_white_cat', '123456', 'The Pince114', 'blue_white_cat', '建筑', '建筑', NULL, NULL, 'STUDENT', 'APPROVED'),
('AllPerfect', '123456', 'allperfect', 'AllPerfect', '建筑', '建筑', NULL, NULL, 'STUDENT', 'APPROVED'),
('doki152', '123456', '乌撒的叶', 'doki152', '建筑', '建筑', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 休闲
('dedw24', '123456', '霸王龙', 'dedw24', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('9527', '123456', '9527', '9527', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('Nikon666', '123456', 'Nikon', 'Nikon666', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('Harry', '123456', 'Harry', 'Harry', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('xiaoze123123123', '123456', '小泽', 'xiaoze123123123', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('yiyun12', '123456', 'yiyun', 'yiyun12', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('jixiaojian', '123456', 'jixiajian', 'jixiaojian', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('creall', '123456', 'creall', 'creall', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('z_z666', '123456', 'z6', 'z_z666', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('wuyu8th', '123456', '无语', 'wuyu8th', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('shiguangyouyou', '123456', '时光悠悠', 'shiguangyouyou', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('summer_Ding', '123456', '夏丁', 'summer_Ding', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 见习
('Buy_stars', '123456', '小星', 'Buy_stars', '见习', '见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('Heary_yang', '123456', 'Harry', 'Heary_yang', '见习', '见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('ltblmxs', '123456', '龙头', 'ltblmxs', '见习', '见习', NULL, NULL, 'STUDENT', 'APPROVED'),
-- pvp
('JD47jx', '123456', '蒸发态', 'JD47jx', 'pvp', 'pvp', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 修仙
('GongS12138', '123456', '宫司', 'GongS12138', '修仙', '修仙', NULL, NULL, 'STUDENT', 'APPROVED'),
-- 其他
('12abc', '123456', '12abc', '12abc', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('younger007', '123456', '小样', 'younger007', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('L_Spled', '123456', '凌', 'L_Spled', '后勤', '后勤', NULL, NULL, 'STUDENT', 'APPROVED'),
('Heary_yang2', '123456', 'Harry2', 'Heary_yang', '见习', '见习', NULL, NULL, 'STUDENT', 'APPROVED'),
('xilian', '123456', '洗脸', 'xilian', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED'),
('www', '123456', 'www', 'www', '休闲', '休闲', NULL, NULL, 'STUDENT', 'APPROVED');

-- 同时把花蝉升级为 Owner
UPDATE admin SET level = 4 WHERE username = 'huachan';
