package com.example.mapper;

import com.example.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamPaperQuestionMapper {

    int insert(ExamPaperQuestion paperQuestion);

    @Select("select * from `exam_paper_question` where id = #{id}")
    ExamPaperQuestion selectById(Integer id);

    @Select("select * from `exam_paper_question` where paper_id = #{paperId} order by question_order")
    List<ExamPaperQuestion> selectByPaperId(Integer paperId);

    @Delete("delete from `exam_paper_question` where paper_id = #{paperId}")
    void deleteByPaperId(Integer paperId);

    @Delete("delete from `exam_paper_question` where id = #{id}")
    void deleteById(Integer id);
}
