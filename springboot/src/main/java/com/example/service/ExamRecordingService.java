package com.example.service;

import com.example.entity.ExamRecording;
import com.example.mapper.ExamRecordingMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamRecordingService {

    @Resource
    private ExamRecordingMapper examRecordingMapper;

    public void add(ExamRecording recording) {
        if (recording.getStatus() == null) {
            recording.setStatus("recording");
        }
        examRecordingMapper.insert(recording);
    }

    public void updateById(ExamRecording recording) {
        examRecordingMapper.updateById(recording);
    }

    public ExamRecording selectById(Integer id) {
        return examRecordingMapper.selectById(id);
    }

    public List<ExamRecording> selectByExamId(Integer examId) {
        return examRecordingMapper.selectByExamId(examId);
    }

    public ExamRecording selectByExamAndStudent(Integer examId, Integer studentId) {
        return examRecordingMapper.selectByExamAndStudent(examId, studentId);
    }

    public List<ExamRecording> selectAll(ExamRecording recording) {
        return examRecordingMapper.selectAll(recording);
    }
}
