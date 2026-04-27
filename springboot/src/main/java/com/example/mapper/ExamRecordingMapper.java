package com.example.mapper;

import com.example.entity.ExamRecording;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamRecordingMapper {

    int insert(ExamRecording recording);

    void updateById(ExamRecording recording);

    @Select("select * from `exam_recording` where id = #{id}")
    ExamRecording selectById(Integer id);

    List<ExamRecording> selectByExamId(Integer examId);

    @Select("select * from `exam_recording` where exam_id = #{examId} and student_id = #{studentId} order by id desc limit 1")
    ExamRecording selectByExamAndStudent(Integer examId, Integer studentId);

    List<ExamRecording> selectAll(ExamRecording recording);
}
