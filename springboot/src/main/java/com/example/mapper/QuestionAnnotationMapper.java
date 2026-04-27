package com.example.mapper;

import com.example.entity.QuestionAnnotation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface QuestionAnnotationMapper {

    int insert(QuestionAnnotation annotation);

    List<QuestionAnnotation> selectByQuestionId(@Param("questionId") Integer questionId);

    @Select("select * from `question_annotation` where id = #{id}")
    QuestionAnnotation selectById(Integer id);

    @Update("update `question_annotation` set like_count = like_count + 1 where id = #{id}")
    void incrementLike(Integer id);

    void deleteById(Integer id);

    @Select("select count(*) from `question_annotation` where question_id = #{questionId} and user_id = #{userId}")
    int countByQuestionAndUser(@Param("questionId") Integer questionId, @Param("userId") Integer userId);
}
