package com.example.mapper;

import com.example.entity.ExamRecord;
import com.example.entity.ExamRecordCompare;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ExamRecordCompareMapper {

    @Insert("INSERT INTO exam_record_compare (exam_id, avg_score, max_score, min_score, total_count, pass_count) " +
             "VALUES (#{examId}, #{avgScore}, #{maxScore}, #{minScore}, #{totalCount}, #{passCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExamRecordCompare record);

    @Select("SELECT * FROM exam_record_compare WHERE exam_id = #{examId} LIMIT 1")
    ExamRecordCompare selectByExamId(@Param("examId") Integer examId);

    @Update("UPDATE exam_record_compare SET avg_score = #{avgScore}, max_score = #{maxScore}, " +
            "min_score = #{minScore}, total_count = #{totalCount}, pass_count = #{passCount} WHERE exam_id = #{examId}")
    void updateByExamId(ExamRecordCompare record);
}
