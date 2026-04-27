package com.example.mapper;

import com.example.entity.StudyCheckin;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface StudyCheckinMapper {
    int insert(StudyCheckin checkin);
    @Select("select * from `study_checkin` where user_id=#{userId} and user_role=#{userRole} and checkin_date=#{date}")
    StudyCheckin selectByUserAndDate(@Param("userId") Integer userId, @Param("userRole") String userRole, @Param("date") LocalDate date);
    @Select("select * from `study_checkin` where user_id=#{userId} and user_role=#{userRole} order by checkin_date desc limit 1")
    StudyCheckin selectLatestByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
    List<StudyCheckin> selectLeaderboard();
    @Select("select count(*) from `study_checkin` where user_id=#{userId} and user_role=#{userRole}")
    int countByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
}
