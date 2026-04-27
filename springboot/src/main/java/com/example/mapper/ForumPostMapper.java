package com.example.mapper;

import com.example.entity.ForumPost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ForumPostMapper {

    int insert(ForumPost post);

    void updateById(ForumPost post);

    void deleteById(Integer id);

    @Select("select * from `forum_post` where id = #{id}")
    ForumPost selectById(Integer id);

    List<ForumPost> selectAll(ForumPost post);

    @Update("update `forum_post` set view_count = view_count + 1 where id = #{id}")
    void incrementViewCount(Integer id);

    @Update("update `forum_post` set comment_count = comment_count + 1 where id = #{id}")
    void incrementCommentCount(Integer id);

    @Update("update `forum_post` set comment_count = comment_count - 1 where id = #{id} and comment_count > 0")
    void decrementCommentCount(Integer id);

    @Update("update `forum_post` set like_count = like_count + #{delta} where id = #{id}")
    void updateLikeCount(@Param("id") Integer id, @Param("delta") int delta);
}
