package com.example.service;

import com.example.entity.ForumPost;
import com.example.mapper.ForumPostMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumPostService {

    @Resource
    private ForumPostMapper forumPostMapper;

    public void add(ForumPost post) {
        if (post.getStatus() == null) post.setStatus("active");
        if (post.getIsTop() == null) post.setIsTop(false);
        if (post.getIsLocked() == null) post.setIsLocked(false);
        if (post.getViewCount() == null) post.setViewCount(0);
        if (post.getLikeCount() == null) post.setLikeCount(0);
        if (post.getCommentCount() == null) post.setCommentCount(0);
        forumPostMapper.insert(post);
    }

    public void updateById(ForumPost post) {
        forumPostMapper.updateById(post);
    }

    public void deleteById(Integer id) {
        forumPostMapper.deleteById(id);
    }

    public ForumPost selectById(Integer id) {
        return forumPostMapper.selectById(id);
    }

    public List<ForumPost> selectAll(ForumPost post) {
        return forumPostMapper.selectAll(post);
    }

    public PageInfo<ForumPost> selectPage(ForumPost post, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ForumPost> list = forumPostMapper.selectAll(post);
        return PageInfo.of(list);
    }

    public void incrementViewCount(Integer id) {
        forumPostMapper.incrementViewCount(id);
    }

    public void incrementCommentCount(Integer id) {
        forumPostMapper.incrementCommentCount(id);
    }

    public void decrementCommentCount(Integer id) {
        forumPostMapper.decrementCommentCount(id);
    }

    public void updateLikeCount(Integer id, int delta) {
        forumPostMapper.updateLikeCount(id, delta);
    }
}
