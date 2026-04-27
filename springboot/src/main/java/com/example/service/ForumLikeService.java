package com.example.service;

import com.example.entity.ForumLike;
import com.example.mapper.ForumLikeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumLikeService {

    @Resource
    private ForumLikeMapper forumLikeMapper;

    /**
     * 切换点赞/收藏状态，返回 true=已点赞, false=已取消
     */
    public boolean toggle(Integer userId, String userRole, String targetType, Integer targetId, String type) {
        ForumLike existing = forumLikeMapper.selectOne(userId, userRole, targetType, targetId, type);
        if (existing != null) {
            forumLikeMapper.delete(userId, userRole, targetType, targetId, type);
            return false;
        } else {
            ForumLike like = new ForumLike();
            like.setUserId(userId);
            like.setUserRole(userRole);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            like.setType(type);
            forumLikeMapper.insert(like);
            return true;
        }
    }

    public boolean check(Integer userId, String userRole, String targetType, Integer targetId, String type) {
        return forumLikeMapper.selectOne(userId, userRole, targetType, targetId, type) != null;
    }

    public List<ForumLike> getFavorites(Integer userId, String userRole) {
        return forumLikeMapper.selectFavorites(userId, userRole);
    }
}
