package com.example.mapper;

import com.example.entity.ExamAnswer;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamAnswerMapper {

    int insert(ExamAnswer answer);

    void updateById(ExamAnswer answer);

    @Select("select * from `exam_answer` where id = #{id}")
    ExamAnswer selectById(Integer id);

    @Select("select * from `exam_answer` where record_id = #{recordId}")
    List<ExamAnswer> selectByRecordId(Integer recordId);

    @Select("select * from `exam_answer` where record_id = #{recordId} and question_id = #{questionId}")
    ExamAnswer selectByRecordAndQuestion(@Param("recordId") Integer recordId,
                                         @Param("questionId") Integer questionId);

    @Delete("delete from `exam_answer` where record_id = #{recordId}")
    void deleteByRecordId(Integer recordId);

    void batchInsert(List<ExamAnswer> answers);

    @Select("select * from `exam_answer` where question_id = #{questionId}")
    List<ExamAnswer> selectByQuestionId(Integer questionId);
}
