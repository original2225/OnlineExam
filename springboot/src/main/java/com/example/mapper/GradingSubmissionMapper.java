package com.example.mapper;

import com.example.entity.GradingSubmission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GradingSubmissionMapper {

    int insert(GradingSubmission submission);

    List<GradingSubmission> selectByRecordId(@Param("recordId") Integer recordId);

    GradingSubmission selectByRecordAndGrader(@Param("recordId") Integer recordId, @Param("graderId") Integer graderId);

    @Select("select count(*) from `grading_submission` where record_id = #{recordId}")
    int countByRecordId(Integer recordId);
}
