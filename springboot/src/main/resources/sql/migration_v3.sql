-- 考试类型字段迁移
ALTER TABLE exam ADD COLUMN IF NOT EXISTS exam_type VARCHAR(20) DEFAULT 'scheduled' COMMENT '考试类型: scheduled(统一考试)/permanent(常驻考试)';

-- student 表新增 branch 列
ALTER TABLE student ADD COLUMN IF NOT EXISTS branch VARCHAR(50) DEFAULT NULL COMMENT '分支';

-- admin 表新增 admin_no 编号列
ALTER TABLE admin ADD COLUMN IF NOT EXISTS admin_no VARCHAR(20) DEFAULT NULL COMMENT '编号';

-- examiner 表新增 examiner_no 编号列
ALTER TABLE examiner ADD COLUMN IF NOT EXISTS examiner_no VARCHAR(20) DEFAULT NULL COMMENT '编号';

-- 阅卷提交表：记录每个阅卷人对同一份试卷的评分
CREATE TABLE IF NOT EXISTS grading_submission (
    id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL COMMENT '考试记录ID',
    grader_id INT NOT NULL COMMENT '阅卷人ID',
    grader_name VARCHAR(50) COMMENT '阅卷人姓名',
    grader_role VARCHAR(20) COMMENT '阅卷人角色: ADMIN/HELPER',
    manual_score DECIMAL(10,2) DEFAULT 0 COMMENT '主观题评分',
    total_score DECIMAL(10,2) DEFAULT 0 COMMENT '总分评分',
    performance_score DECIMAL(5,2) NULL COMMENT '玩家表现评分',
    advisory_vote VARCHAR(20) NULL COMMENT '参考表决: PASS/FAIL/ABSTAIN',
    rejection_reasons VARCHAR(500) NULL COMMENT '不通过原因，多选逗号分隔',
    custom_reason VARCHAR(500) NULL COMMENT '自定义不通过原因',
    comment TEXT COMMENT '评语',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_record_grader (record_id, grader_id)
) COMMENT '阅卷提交记录';
