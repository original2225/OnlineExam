package com.example.mapper;

import com.example.entity.AchievementRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AchievementRecordMapper {

    @Insert("INSERT INTO achievement_record (user_id, user_name, user_role, achievement_key, achievement_name, description, icon, category, score, earned_at, notified) " +
             "VALUES (#{userId}, #{userName}, #{userRole}, #{achievementKey}, #{achievementName}, #{description}, #{icon}, #{category}, #{score}, #{earnedAt}, #{notified})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AchievementRecord record);

    @Select("SELECT * FROM achievement_record WHERE user_id = #{userId} AND user_role = #{userRole} AND achievement_key = #{achievementKey} LIMIT 1")
    AchievementRecord selectByUserAndKey(@Param("userId") Integer userId, @Param("userRole") String userRole, @Param("achievementKey") String achievementKey);

    @Select("SELECT * FROM achievement_record WHERE user_id = #{userId} AND user_role = #{userRole} ORDER BY earned_at DESC")
    List<AchievementRecord> selectByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);

    @Select("SELECT * FROM achievement_record ORDER BY earned_at DESC")
    List<AchievementRecord> selectAll();

    @Select("SELECT COUNT(*) FROM achievement_record WHERE user_id = #{userId} AND user_role = #{userRole}")
    Integer countByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);

    @Select("SELECT COUNT(*) FROM achievement_record")
    Integer countAll();

    @Select("SELECT * FROM achievement_record ORDER BY earned_at DESC LIMIT #{limit}")
    List<AchievementRecord> selectRecent(@Param("limit") Integer limit);

    @Update("UPDATE achievement_record SET notified = true WHERE id = #{id}")
    void markNotified(@Param("id") Integer id);
}
