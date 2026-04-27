package com.example.mapper;

import com.example.entity.ExamAnnouncement;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamAnnouncementMapper {

    int insert(ExamAnnouncement announcement);

    void updateById(ExamAnnouncement announcement);

    void deleteById(Integer id);

    @Select("select * from `exam_announcement` where id = #{id}")
    ExamAnnouncement selectById(Integer id);

    List<ExamAnnouncement> selectAll(ExamAnnouncement announcement);

    List<ExamAnnouncement> selectPublished();

    List<ExamAnnouncement> selectByExamId(Integer examId);
}
