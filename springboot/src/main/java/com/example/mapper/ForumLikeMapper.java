package com.example.mapper;

import com.example.entity.ForumLike;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ForumLikeMapper {

    int insert(ForumLike like);

    void delete(@Param("userId") Integer userId, @Param("userRole") String userRole,
                @Param("targetType") String targetType, @Param("targetId") Integer targetId,
                @Param("type") String type);

    @Select("select * from `forum_like` where user_id = #{userId} and user_role = #{userRole} " +
            "and target_type = #{targetType} and target_id = #{targetId} and type = #{type}")
    ForumLike selectOne(@Param("userId") Integer userId, @Param("userRole") String userRole,
                        @Param("targetType") String targetType, @Param("targetId") Integer targetId,
                        @Param("type") String type);

    @Select("select * from `forum_like` where user_id = #{userId} and user_role = #{userRole} and type = 'favorite' order by created_at desc")
    List<ForumLike> selectFavorites(@Param("userId") Integer userId, @Param("userRole") String userRole);
}
