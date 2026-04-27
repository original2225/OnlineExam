package com.example.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 考试成绩对比实体（用于存储考试的统计数据）
 */
@Data
public class ExamRecordCompare {
    private Integer id;
    /** 考试ID */
    private Integer examId;
    /** 平均分 */
    private BigDecimal avgScore;
    /** 最高分 */
    private BigDecimal maxScore;
    /** 最低分 */
    private BigDecimal minScore;
    /** 参考人数 */
    private Integer totalCount;
    /** 及格人数 */
    private Integer passCount;
}
