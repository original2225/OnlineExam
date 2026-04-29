package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.ExamRecord;
import com.example.entity.ExamRecordChatMessage;
import com.example.entity.ExamRecordChatRoom;
import com.example.entity.Examiner;
import com.example.entity.Student;
import com.example.mapper.AdminMapper;
import com.example.mapper.ExaminerMapper;
import com.example.mapper.ExamRecordChatMapper;
import com.example.mapper.ExamRecordMapper;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ExamRecordChatService {
    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());
    private static final Set<String> FINAL_STATUSES = Set.of("PASSED", "FAILED");

    @Resource
    private ExamRecordChatMapper examRecordChatMapper;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private ExaminerMapper examinerMapper;

    @Resource
    private NotificationService notificationService;

    public List<ExamRecordChatMessage> messages(Integer recordId, Integer afterId) {
        Account current = requireCurrent();
        ensureCanRead(recordId, current);
        List<ExamRecordChatMessage> messages = examRecordChatMapper.selectMessages(recordId, afterId);
        if (!messages.isEmpty()) {
            read(recordId, messages.get(messages.size() - 1).getId());
        }
        return messages;
    }

    @Transactional
    public ExamRecordChatMessage send(Integer recordId, String content) {
        Account current = requireCurrent();
        ExamRecord record = ensureCanRead(recordId, current);
        if (isLocked(record)) {
            throw new IllegalArgumentException("追问室已归档，无法继续发送消息");
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (text.length() > 1000) {
            throw new IllegalArgumentException("消息不能超过1000字");
        }

        ExamRecordChatMessage message = new ExamRecordChatMessage();
        message.setRecordId(recordId);
        message.setSenderId(current.getId());
        message.setSenderRole(current.getRole());
        message.setSenderName(displayName(current));
        message.setContent(text);
        message.setMessageType("text");
        message.setIsDeleted(false);
        examRecordChatMapper.insertMessage(message);
        examRecordChatMapper.upsertRead(recordId, current.getId(), current.getRole(), message.getId());
        notifyReceivers(record, current, text);
        return message;
    }

    public void read(Integer recordId, Integer messageId) {
        Account current = requireCurrent();
        ensureCanRead(recordId, current);
        Integer targetMessageId = messageId;
        if (targetMessageId == null || targetMessageId <= 0) {
            targetMessageId = examRecordChatMapper.selectLastMessageId(recordId);
        }
        if (targetMessageId != null && targetMessageId > 0) {
            examRecordChatMapper.upsertRead(recordId, current.getId(), current.getRole(), targetMessageId);
        }
    }

    public int unreadCount(Integer recordId) {
        Account current = requireCurrent();
        ensureCanRead(recordId, current);
        return examRecordChatMapper.unreadCount(recordId, current.getId(), current.getRole());
    }

    public List<ExamRecordChatRoom> myRooms() {
        Account current = requireCurrent();
        boolean isReviewer = REVIEW_ROLES.contains(current.getRole());
        return examRecordChatMapper.selectRooms(current.getId(), current.getRole(), isReviewer);
    }

    private Account requireCurrent() {
        Account current = TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null || current.getRole() == null) {
            throw new SecurityException("请先登录");
        }
        return current;
    }

    private ExamRecord ensureCanRead(Integer recordId, Account current) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("答卷不存在");
        }
        if (!"completed".equals(record.getStatus())) {
            throw new SecurityException("答卷提交后才可进入追问室");
        }
        if (REVIEW_ROLES.contains(current.getRole())) {
            return record;
        }
        if (RoleEnum.USER.name().equals(current.getRole()) && current.getId().equals(record.getStudentId())) {
            return record;
        }
        throw new SecurityException("无权限访问该追问室");
    }

    private boolean isLocked(ExamRecord record) {
        return record.getExamStatus() != null && FINAL_STATUSES.contains(record.getExamStatus());
    }

    private void notifyReceivers(ExamRecord record, Account sender, String content) {
        String preview = content.length() > 48 ? content.substring(0, 48) + "..." : content;
        String title = "答卷追问室有新消息";
        String linkForStudent = "/front/myScores?chatRecordId=" + record.getId();
        String linkForReviewer = "/exam/gradingCenter?chatRecordId=" + record.getId() + "&examId=" + record.getExamId();

        if (RoleEnum.USER.name().equals(sender.getRole())) {
            String message = displayName(sender) + "： " + preview;
            for (Admin admin : adminMapper.selectAll(new Admin())) {
                if (admin.getId() != null && REVIEW_ROLES.contains(admin.getRole())) {
                    notificationService.send(admin.getId(), admin.getRole(), title, message, "exam_chat", linkForReviewer);
                }
            }
            for (Examiner examiner : examinerMapper.selectAll(new Examiner())) {
                if (examiner.getId() != null) {
                    notificationService.send(examiner.getId(), RoleEnum.HELPER.name(), title, message, "exam_chat", linkForReviewer);
                }
            }
        } else {
            notificationService.send(record.getStudentId(), RoleEnum.USER.name(), title,
                    displayName(sender) + "： " + preview, "exam_chat", linkForStudent);
        }
    }

    private String displayName(Account account) {
        if (account instanceof Admin admin && admin.getName() != null && !admin.getName().isBlank()) {
            return admin.getName();
        }
        if (account instanceof Examiner examiner && examiner.getName() != null && !examiner.getName().isBlank()) {
            return examiner.getName();
        }
        if (account instanceof Student student && student.getName() != null && !student.getName().isBlank()) {
            return student.getName();
        }
        return account.getUsername() == null ? "成员" : account.getUsername();
    }
}
