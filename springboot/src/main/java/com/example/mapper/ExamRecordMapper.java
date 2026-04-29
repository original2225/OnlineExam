package com.example.mapper;

import com.example.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ExamRecordMapper {

    int insert(ExamRecord record);

    void updateById(ExamRecord record);

    void deleteById(Integer id);

    @Select("select * from `exam_record` where id = #{id}")
    ExamRecord selectById(Integer id);

    @Select("select * from `exam_record` where exam_id = #{examId} and student_id = #{studentId} and status = 'ongoing'")
    ExamRecord selectOngoingRecord(@Param("examId") Integer examId,
                                   @Param("studentId") Integer studentId);

    List<ExamRecord> selectAll(ExamRecord record);

    List<ExamRecord> selectByExamId(Integer examId);

    List<ExamRecord> selectByStudentId(Integer studentId);

    List<ExamRecord> selectPublicResults();

    List<Map<String, Object>> selectEntryReviewLeaderboard();

    Map<String, Object> getGradingProgress(Integer examId);

    void unpublishByExamId(@Param("examId") Integer examId);

    void publishByExamId(@Param("examId") Integer examId, @Param("publishedAt") java.time.LocalDateTime publishedAt);
}
