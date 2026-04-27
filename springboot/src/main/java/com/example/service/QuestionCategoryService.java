package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.QuestionCategory;
import com.example.exception.CustomException;
import com.example.mapper.QuestionCategoryMapper;
import com.example.mapper.QuestionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目分类业务层
 */
@Service
public class QuestionCategoryService {

    @Resource
    private QuestionCategoryMapper questionCategoryMapper;

    @Resource
    private QuestionMapper questionMapper;

    public void add(QuestionCategory category) {
        // 检查分类名称是否重复
        QuestionCategory dbCategory = questionCategoryMapper.selectById(category.getId());
        if (ObjectUtil.isNotNull(dbCategory)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        if (ObjectUtil.isEmpty(category.getStatus())) {
            category.setStatus("active");
        }
        questionCategoryMapper.insert(category);
    }

    public void updateById(QuestionCategory category) {
        questionCategoryMapper.updateById(category);
    }

    public void deleteById(Integer id) {
        // 检查是否有子分类
        List<QuestionCategory> children = questionCategoryMapper.selectByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new CustomException(ResultCodeEnum.CATEGORY_HAS_CHILDREN);
        }
        // 检查是否有题目关联
        int questionCount = questionMapper.countByCategoryId(id);
        if (questionCount > 0) {
            throw new CustomException(ResultCodeEnum.CATEGORY_HAS_QUESTIONS);
        }
        questionCategoryMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public QuestionCategory selectById(Integer id) {
        return questionCategoryMapper.selectById(id);
    }

    public List<QuestionCategory> selectAll(QuestionCategory category) {
        return questionCategoryMapper.selectAll(category);
    }

    public PageInfo<QuestionCategory> selectPage(QuestionCategory category, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<QuestionCategory> list = questionCategoryMapper.selectAll(category);
        return PageInfo.of(list);
    }
}
