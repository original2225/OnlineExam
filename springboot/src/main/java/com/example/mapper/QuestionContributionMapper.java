package com.example.mapper;

import com.example.entity.QuestionContribution;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionContributionMapper {
    int insert(QuestionContribution qc);
    void updateById(QuestionContribution qc);
    QuestionContribution selectById(Integer id);
    List<QuestionContribution> selectAll(@Param("status") String status);
    List<QuestionContribution> selectByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
}
