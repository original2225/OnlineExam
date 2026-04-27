package com.example.mapper;

import com.example.entity.ForumComment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ForumCommentMapper {

    int insert(ForumComment comment);

    void deleteById(Integer id);

    @Select("select * from `forum_comment` where id = #{id}")
    ForumComment selectById(Integer id);

    List<ForumComment> selectByPostId(@Param("postId") Integer postId);

    @Update("update `forum_comment` set like_count = like_count + #{delta} where id = #{id}")
    void updateLikeCount(@Param("id") Integer id, @Param("delta") int delta);
}
