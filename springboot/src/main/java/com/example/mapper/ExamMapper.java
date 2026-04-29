package com.example.mapper;

import com.example.entity.Exam;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamMapper {

    int insert(Exam exam);

    void updateById(Exam exam);

    void deleteById(Integer id);

    @Select("select * from `exam` where id = #{id}")
    Exam selectById(Integer id);

    List<Exam> selectAll(Exam exam);

    void updateStatus(@Param("id") Integer id, @Param("status") String status);
}
