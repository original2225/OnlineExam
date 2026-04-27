package com.example.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.AdminService;
import com.example.service.ExaminerService;
import com.example.service.StudentService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * Token工具类
 */
@Component
public class TokenUtils {
    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    @Resource
    private AdminService adminService;
    @Resource
    private StudentService studentService;
    @Resource
    private ExaminerService examinerService;

    @Value("${app.jwt.secret:beiming-online-exam-dev-secret-change-me}")
    private String jwtSecret;

    private static AdminService staticAdminService;
    private static StudentService staticStudentService;
    private static ExaminerService staticExaminerService;
    private static String staticJwtSecret = "beiming-online-exam-dev-secret-change-me";

    @PostConstruct
    public void init() {
        staticAdminService = adminService;
        staticStudentService = studentService;
        staticExaminerService = examinerService;
        staticJwtSecret = jwtSecret;
    }

    /**
     * 生成JWT token
     */
    public static String createToken(String data) {
        return JWT.create().withAudience(data)
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .sign(Algorithm.HMAC256(staticJwtSecret));
    }

    public static String getJwtSecret() {
        return staticJwtSecret;
    }

    /**
     * 获取当前登录的用户
     */
    public static Account getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.TOKEN);
            String audience = JWT.decode(token).getAudience().get(0);
            String[] userRole = audience.split("-");
            Integer userId = Integer.valueOf(userRole[0]);
            String role = userRole[1];
            if (RoleEnum.OWNER.name().equals(role) || RoleEnum.ADMIN.name().equals(role)) {
                return staticAdminService.selectById(userId);
            }
            if (RoleEnum.USER.name().equals(role)) {
                return staticStudentService.selectById(userId);
            }
            if (RoleEnum.HELPER.name().equals(role)) {
                return staticExaminerService.selectById(userId);
            }
        } catch (Exception e) {
            log.error("获取当前登录用户出错", e);
        }
        return null;
    }

}
