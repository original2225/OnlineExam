package com.example.mapper;

import com.example.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NotificationMapper {
    int insert(Notification notification);
    List<Notification> selectByUser(@Param("userId") Integer userId, @Param("userRole") String userRole);
    @Select("select * from `notification` where id = #{id}")
    Notification selectById(Integer id);
    @Update("update `notification` set is_read = 1 where id = #{id}")
    void markRead(Integer id);
    @Update("update `notification` set is_read = 1 where user_id = #{userId} and user_role = #{userRole}")
    void markAllRead(@Param("userId") Integer userId, @Param("userRole") String userRole);
    @Select("select count(*) from `notification` where user_id=#{userId} and user_role=#{userRole} and is_read=0")
    int unreadCount(@Param("userId") Integer userId, @Param("userRole") String userRole);
}
