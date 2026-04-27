package com.example.controller;

import com.example.common.Result;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationMapper notificationMapper;

    @GetMapping("/selectByUser")
    public Result selectByUser(@RequestParam Integer userId, @RequestParam String userRole) {
        List<Notification> list = notificationMapper.selectByUser(userId, userRole);
        return Result.success(list);
    }

    @PutMapping("/read/{id}")
    public Result read(@PathVariable Integer id) {
        notificationMapper.markRead(id);
        return Result.success();
    }

    @PutMapping("/readAll")
    public Result readAll(@RequestParam Integer userId, @RequestParam String userRole) {
        notificationMapper.markAllRead(userId, userRole);
        return Result.success();
    }

    @GetMapping("/unreadCount")
    public Result unreadCount(@RequestParam Integer userId, @RequestParam String userRole) {
        int count = notificationMapper.unreadCount(userId, userRole);
        return Result.success(count);
    }

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, Object> params) {
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String type = (String) params.getOrDefault("type", "system");
        String link = (String) params.get("link");
        List<Number> userIds = (List<Number>) params.get("userIds");
        String userRole = (String) params.get("userRole");

        if (userIds != null) {
            for (Number uid : userIds) {
                Notification n = new Notification();
                n.setUserId(uid.intValue());
                n.setUserRole(userRole);
                n.setTitle(title);
                n.setContent(content);
                n.setType(type);
                n.setLink(link);
                n.setIsRead(false);
                notificationMapper.insert(n);
            }
        }
        return Result.success();
    }

    /** Helper: send notification to a single user */
    public static void sendNotification(NotificationMapper mapper, Integer userId, String userRole,
                                         String title, String content, String type, String link) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setUserRole(userRole);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setLink(link);
        n.setIsRead(false);
        mapper.insert(n);
    }
}
