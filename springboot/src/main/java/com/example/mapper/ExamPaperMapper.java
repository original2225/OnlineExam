package com.example.mapper;

import com.example.entity.ExamPaper;
import com.example.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamPaperMapper {

    int insert(ExamPaper paper);

    void updateById(ExamPaper paper);

    void deleteById(Integer id);

    @Select("select * from `exam_paper` where id = #{id}")
    ExamPaper selectById(Integer id);

    List<ExamPaper> selectAll(ExamPaper paper);

    /**
     * 删除试卷的所有题目
     */
    void deleteQuestions(Integer paperId);

    /**
     * 批量插入试卷题目
     */
    void batchInsertQuestions(List<ExamPaperQuestion> questions);
}
