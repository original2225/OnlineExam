package com.example.controller;

import com.example.common.Result;
import com.example.service.ExamRecordChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/examRecordChat")
public class ExamRecordChatController {

    @Resource
    private ExamRecordChatService examRecordChatService;

    @GetMapping("/messages")
    public Result messages(@RequestParam Integer recordId,
                           @RequestParam(required = false) Integer afterId) {
        try {
            return Result.success(examRecordChatService.messages(recordId, afterId));
        } catch (SecurityException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, Object> body) {
        try {
            Integer recordId = Integer.valueOf(String.valueOf(body.get("recordId")));
            String content = String.valueOf(body.getOrDefault("content", ""));
            return Result.success(examRecordChatService.send(recordId, content));
        } catch (SecurityException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.error(e.getMessage() != null ? e.getMessage() : "参数错误");
        }
    }

    @PutMapping("/read")
    public Result read(@RequestParam Integer recordId,
                       @RequestParam(required = false) Integer messageId) {
        try {
            examRecordChatService.read(recordId, messageId);
            return Result.success();
        } catch (SecurityException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/unreadCount")
    public Result unreadCount(@RequestParam Integer recordId) {
        try {
            return Result.success(examRecordChatService.unreadCount(recordId));
        } catch (SecurityException e) {
            return forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/myRooms")
    public Result myRooms() {
        try {
            return Result.success(examRecordChatService.myRooms());
        } catch (SecurityException e) {
            return forbidden(e.getMessage());
        }
    }

    private Result forbidden(String message) {
        return Result.error("403", message == null ? "无权限访问" : message);
    }
}
