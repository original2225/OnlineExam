package com.example.mapper;

import com.example.entity.QuestionCategory;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionCategoryMapper {

    int insert(QuestionCategory category);

    void updateById(QuestionCategory category);

    void deleteById(Integer id);

    @Select("select * from `question_category` where id = #{id}")
    QuestionCategory selectById(Integer id);

    List<QuestionCategory> selectAll(QuestionCategory category);

    List<QuestionCategory> selectByParentId(Integer parentId);
}
