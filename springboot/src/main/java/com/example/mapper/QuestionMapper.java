package com.example.mapper;

import com.example.entity.Question;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionMapper {

    int insert(Question question);

    void updateById(Question question);

    void deleteById(Integer id);

    @Select("select * from `question` where id = #{id}")
    Question selectById(Integer id);

    @Select("select count(*) from `question` where category_id = #{categoryId}")
    int countByCategoryId(Integer categoryId);

    List<Question> selectAll(Question question);

    List<Question> selectByPaperId(Integer paperId);
}
