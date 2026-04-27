package com.example.service;

import com.example.entity.Tutorial;
import com.example.mapper.TutorialMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TutorialService {

    @Resource
    private TutorialMapper tutorialMapper;

    public void add(Tutorial tutorial) {
        tutorialMapper.insert(tutorial);
    }

    public void updateById(Tutorial tutorial) {
        tutorialMapper.updateById(tutorial);
    }

    public void deleteById(Integer id) {
        tutorialMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            tutorialMapper.deleteById(id);
        }
    }

    public Tutorial selectById(Integer id) {
        return tutorialMapper.selectById(id);
    }

    public List<Tutorial> selectAll(Tutorial tutorial) {
        return tutorialMapper.selectAll(tutorial);
    }

    public PageInfo<Tutorial> selectPage(Tutorial tutorial, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Tutorial> list = tutorialMapper.selectAll(tutorial);
        return PageInfo.of(list);
    }

    public void incrementViewCount(Integer id) {
        tutorialMapper.incrementViewCount(id);
    }
}
