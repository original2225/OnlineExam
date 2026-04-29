package com.example.mapper;

import com.example.entity.WrongQuestion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WrongQuestionMapper {

    int insert(WrongQuestion wrongQuestion);

    List<WrongQuestion> selectByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);

    void deleteById(Integer id);

    void deleteByOwner(@Param("id") Integer id, @Param("userId") Integer userId, @Param("userRole") String userRole);

    @Select("select count(*) from `wrong_question` where user_id = #{userId} and user_role = #{userRole} and question_id = #{questionId}")
    int existsByUserAndQuestion(@Param("userId") Integer userId, @Param("userRole") String userRole, @Param("questionId") Integer questionId);

    @Select("select count(*) from `wrong_question` where user_id = #{userId} and user_role = #{userRole}")
    int countByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
}
