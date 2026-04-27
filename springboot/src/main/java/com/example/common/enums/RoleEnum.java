package com.example.common.enums;

import com.example.entity.Examiner;

public enum RoleEnum {
    // 所有者 - 权限4
    OWNER,
    // 管理员 - 权限3
    ADMIN,
    // 阅卷人 - 权限2
    HELPER,
    // 用户 - 权限1
    USER;
}
