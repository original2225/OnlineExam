package com.example.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.ExaminerService;
import com.example.service.StudentService;
import com.example.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class JWTInterceptor implements HandlerInterceptor {

    private static final Set<String> ADMIN_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name());
    private static final Set<String> REVIEW_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name());
    private static final Set<String> ALL_ROLES = Set.of(RoleEnum.OWNER.name(), RoleEnum.ADMIN.name(), RoleEnum.HELPER.name(), RoleEnum.USER.name());

    @Resource
    private AdminService adminService;

    @Resource
    private ExaminerService examinerService;

    @Resource
    private StudentService studentService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader(Constants.TOKEN);
        if (ObjectUtil.isNull(token)) {
            token = request.getParameter(Constants.TOKEN);
        }
        if (ObjectUtil.isNull(token)) {
            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
        }

        Account account;
        String role;
        try {
            String audience = JWT.decode(token).getAudience().get(0);
            String[] parts = audience.split("-");
            String userId = parts[0];
            role = parts[1];
            if (RoleEnum.OWNER.name().equals(role) || RoleEnum.ADMIN.name().equals(role)) {
                account = adminService.selectById(Integer.valueOf(userId));
            } else if (RoleEnum.HELPER.name().equals(role)) {
                account = examinerService.selectById(Integer.valueOf(userId));
            } else if (RoleEnum.USER.name().equals(role)) {
                account = studentService.selectById(Integer.valueOf(userId));
            } else {
                throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
            }
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }

        if (ObjectUtil.isNull(account)) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TokenUtils.getJwtSecret())).build();
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
        }

        checkPermission(request.getServletPath(), request.getMethod(), role);
        return true;
    }

    private void checkPermission(String path, String method, String role) {
        if (!ALL_ROLES.contains(role)) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN);
        }
        if (path.startsWith("/admin") || path.startsWith("/unifiedUser") || path.startsWith("/registration")
                || path.startsWith("/invitationCode") || path.startsWith("/promotion")) {
            require(role, ADMIN_ROLES);
        }
        if (path.startsWith("/student") || path.startsWith("/examiner")) {
            if (!"GET".equalsIgnoreCase(method) || path.endsWith("selectAll") || path.contains("selectPage")) {
                require(role, ADMIN_ROLES);
            }
        }
        if (matches(path, "/questionContribution")) {
            if (path.contains("/review") || path.contains("/selectPage") || path.contains("/pendingCount")) {
                require(role, ADMIN_ROLES);
            } else if (!"GET".equalsIgnoreCase(method) && !path.contains("/submit")) {
                require(role, ADMIN_ROLES);
            }
            return;
        }
        if (matches(path, "/question") || matches(path, "/examPaper")) {
            if (!"GET".equalsIgnoreCase(method)) {
                require(role, ADMIN_ROLES);
            }
        }
        if (matches(path, "/examRecord")) {
            if (path.contains("/selectAll") || path.contains("/selectPage") || path.contains("/getByExamId")
                    || path.contains("/gradingStats") || path.contains("/update")) {
                require(role, REVIEW_ROLES);
            }
            return;
        }
        if (matches(path, "/examRecording")) {
            if (path.contains("/selectByExam/") || path.contains("/selectAll")) {
                require(role, REVIEW_ROLES);
            }
            return;
        }
        if (matches(path, "/notification")) {
            if (path.contains("/send")) {
                require(role, ADMIN_ROLES);
            }
            return;
        }
        if (matches(path, "/exam")) {
            if (!"GET".equalsIgnoreCase(method) || path.contains("setPermissions") || path.contains("makeup")) {
                require(role, ADMIN_ROLES);
            }
        }
        if (matches(path, "/grading") || matches(path, "/examApproval")) {
            require(role, REVIEW_ROLES);
        }
        if (matches(path, "/score") && (path.contains("selectPage") || path.contains("getStatistics"))) {
            require(role, REVIEW_ROLES);
        }
        if (path.equals("/files/upload")) {
            require(role, ALL_ROLES);
        }
    }

    private boolean matches(String path, String prefix) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private void require(String role, Set<String> allowedRoles) {
        if (!allowedRoles.contains(role)) {
            throw new CustomException(ResultCodeEnum.FORBIDDEN);
        }
    }
}
