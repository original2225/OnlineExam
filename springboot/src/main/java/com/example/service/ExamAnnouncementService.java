package com.example.service;

import com.example.entity.ExamAnnouncement;
import com.example.mapper.ExamAnnouncementMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAnnouncementService {

    @Resource
    private ExamAnnouncementMapper examAnnouncementMapper;

    public void add(ExamAnnouncement announcement) {
        if (announcement.getStatus() == null) {
            announcement.setStatus("published");
        }
        examAnnouncementMapper.insert(announcement);
    }

    public void updateById(ExamAnnouncement announcement) {
        examAnnouncementMapper.updateById(announcement);
    }

    public void deleteById(Integer id) {
        examAnnouncementMapper.deleteById(id);
    }

    public ExamAnnouncement selectById(Integer id) {
        return examAnnouncementMapper.selectById(id);
    }

    public List<ExamAnnouncement> selectAll(ExamAnnouncement announcement) {
        return examAnnouncementMapper.selectAll(announcement);
    }

    public List<ExamAnnouncement> selectPublished() {
        return examAnnouncementMapper.selectPublished();
    }

    public List<ExamAnnouncement> selectByExamId(Integer examId) {
        return examAnnouncementMapper.selectByExamId(examId);
    }
}
