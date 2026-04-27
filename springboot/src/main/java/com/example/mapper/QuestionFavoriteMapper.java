package com.example.mapper;

import com.example.entity.QuestionFavorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionFavoriteMapper {
    int insert(QuestionFavorite fav);
    void deleteById(Integer id);
    List<QuestionFavorite> selectByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
    @Select("select count(*) from `question_favorite` where user_id=#{userId} and user_role=#{userRole} and question_id=#{questionId}")
    int existsByUserAndQuestion(@Param("userId") Integer userId, @Param("userRole") String userRole, @Param("questionId") Integer questionId);
}
