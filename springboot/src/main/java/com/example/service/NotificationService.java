package com.example.service;

import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 通知业务层
 */
@Service
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    /**
     * 发送通知给单个用户
     */
    public void send(Integer userId, String userRole, String title, String content, String type, String link) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setUserRole(userRole);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setLink(link);
        n.setIsRead(false);
        notificationMapper.insert(n);
    }

    /**
     * 发送通知给多个用户
     */
    public void sendBatch(java.util.List<Integer> userIds, String userRole, String title, String content, String type, String link) {
        for (Integer userId : userIds) {
            send(userId, userRole, title, content, type, link);
        }
    }

    /**
     * 发送考试提交通知给管理员
     */
    public void notifyExamSubmitted(String studentName, String examName) {
        send(1, "ADMIN", "新考试提交", studentName + " 提交了「" + examName + "」", "exam", "/exam/gradingCenter");
    }

    /**
     * 发送成绩发布通知
     */
    public void notifyScorePublished(String studentName, String examName, boolean passed) {
        String title = passed ? "恭喜通过考试！" : "考试成绩已发布";
        String content = "您的「" + examName + "」成绩已发布，请前往查看。";
        send(1, "ADMIN", title, content, "score", "/exam/results");
    }

    /**
     * 发送考核通过通知给学生
     */
    public void notifyApprovalPassed(Integer studentId, String examName) {
        send(studentId, "USER", "考核通过", "恭喜！你通过了「" + examName + "」的考核", "approval", "/front/myScores");
    }

    /**
     * 发送考核未通过通知给学生
     */
    public void notifyApprovalFailed(Integer studentId, String examName) {
        send(studentId, "USER", "考核未通过", "「" + examName + "」考核未通过，请继续努力", "approval", "/front/myScores");
    }
}
