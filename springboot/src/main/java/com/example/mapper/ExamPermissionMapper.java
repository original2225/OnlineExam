package com.example.mapper;

import com.example.entity.ExamPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamPermissionMapper {

    int insert(ExamPermission permission);

    @Select("select * from `exam_permission` where id = #{id}")
    ExamPermission selectById(Integer id);

    @Select("select * from `exam_permission` where exam_id = #{examId}")
    List<ExamPermission> selectByExamId(Integer examId);

    @Select("select * from `exam_permission` where student_id = #{studentId}")
    List<ExamPermission> selectByStudentId(Integer studentId);

    @Delete("delete from `exam_permission` where id = #{id}")
    void deleteById(Integer id);

    @Delete("delete from `exam_permission` where exam_id = #{examId}")
    void deleteByExamId(Integer examId);

    void batchInsert(List<ExamPermission> permissions);

    List<ExamPermission> selectAll(ExamPermission permission);
}
