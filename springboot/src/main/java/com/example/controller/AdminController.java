package com.example.controller;

import com.auth0.jwt.JWT;
import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.Examiner;
import com.example.entity.Student;
import com.example.entity.UnifiedUser;
import com.example.mapper.AdminMapper;
import com.example.mapper.ExaminerMapper;
import com.example.mapper.StudentMapper;
import com.example.service.AdminService;
import com.example.service.ExaminerService;
import com.example.service.StudentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前端请求接口
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private ExaminerService examinerService;

    @Resource
    private StudentService studentService;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private ExaminerMapper examinerMapper;

    @Resource
    private StudentMapper studentMapper;

    /**
     * 用户身份转移：从旧表删除，插入新表，保留密码等核心字段
     */
    @PostMapping("/transferUser")
    public Result transferUser(@RequestBody Map<String, Object> form, HttpServletRequest request) {
        // 权限校验：Helper(level>=2) 可转移到阅卷人，Owner(level>=4) 可转移到任意身份
        Admin operator = null;
        try {
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                return Result.error("401", "未授权");
            }
            String audience = JWT.decode(token).getAudience().get(0);
            String operatorId = audience.split("-")[0];
            String operatorRole = audience.split("-")[1];
            if (!"OWNER".equals(operatorRole) && !"ADMIN".equals(operatorRole)) {
                return Result.error("403", "无权操作，需要管理员身份");
            }
            operator = adminService.selectById(Integer.valueOf(operatorId));
            if (operator == null || operator.getLevel() < 3) {
                return Result.error("403", "无权操作");
            }
        } catch (Exception e) {
            return Result.error("401", "认证失败");
        }

        String newSource = (String) form.get("source");
        // 权限校验：只能提拔/降级到比自己低的等级
        int operatorLevel = operator.getLevel() != null ? operator.getLevel() : 0;
        int targetLevel = getLevelBySource(newSource);
        if (targetLevel >= operatorLevel) {
            return Result.error("403", "权限不足，只能将用户调整到比自己低的权限等级");
        }

        Integer userId = Integer.valueOf(form.get("id").toString());
        String originalSource = (String) form.get("_originalSource");

        if (originalSource == null || newSource == null || originalSource.equals(newSource)) {
            return Result.error("参数错误");
        }

        // 1. 从原表读取用户数据（保留密码、用户名等）
        String password = null;
        String username = null;
        String preservedName = null;
        String preservedAvatar = null;
        String preservedPhone = null;
        String preservedEmail = null;
        String preservedUserNo = null;
        String preservedBranch = null;
        String preservedClassName = null;
        String preservedStatus = null;

        switch (originalSource) {
            case "OWNER":
            case "ADMIN": {
                Admin a = adminService.selectById(userId);
                if (a == null) return Result.error("用户不存在");
                password = a.getPassword();
                username = a.getUsername();
                preservedName = a.getName();
                preservedAvatar = a.getAvatar();
                preservedPhone = a.getPhone();
                preservedEmail = a.getEmail();
                preservedUserNo = a.getAdminNo();
                adminService.deleteById(userId);
                break;
            }
            case "HELPER": {
                Examiner e = examinerService.selectById(userId);
                if (e == null) return Result.error("用户不存在");
                password = e.getPassword();
                username = e.getUsername();
                preservedName = e.getName();
                preservedAvatar = e.getAvatar();
                preservedPhone = e.getPhone();
                preservedEmail = e.getEmail();
                preservedUserNo = e.getExaminerNo();
                examinerService.deleteById(userId);
                break;
            }
            case "USER": {
                Student s = studentService.selectById(userId);
                if (s == null) return Result.error("用户不存在");
                password = s.getPassword();
                username = s.getUsername();
                preservedName = s.getName();
                preservedAvatar = s.getAvatar();
                preservedPhone = s.getPhone();
                preservedEmail = s.getEmail();
                preservedUserNo = s.getStudentNo();
                preservedBranch = s.getBranch();
                preservedClassName = s.getClassName();
                preservedStatus = s.getStatus();
                studentService.deleteById(userId);
                break;
            }
            default:
                return Result.error("未知的原始身份");
        }

        // 合并表单值与保留值
        String name = getOrDefault(form, "name", preservedName);
        String avatar = getOrDefault(form, "avatar", preservedAvatar);
        String phone = getOrDefault(form, "phone", preservedPhone);
        String email = getOrDefault(form, "email", preservedEmail);
        String userNo = getOrDefault(form, "userNo", preservedUserNo);
        String branch = getOrDefault(form, "branch", preservedBranch);
        String className = getOrDefault(form, "className", preservedClassName);
        String status = getOrDefault(form, "status", preservedStatus);

        // 2. 插入新表
        if ("OWNER".equals(newSource) || "ADMIN".equals(newSource)) {
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setName(name);
            admin.setAvatar(avatar);
            admin.setPhone(phone);
            admin.setEmail(email);
            admin.setAdminNo(userNo);
            int level = "OWNER".equals(newSource) ? 4 : 3;
            admin.setLevel(level);
            if (level >= 4) {
                admin.setRole("OWNER");
            } else {
                admin.setRole("ADMIN");
            }
            adminMapper.insert(admin);
        } else if ("HELPER".equals(newSource)) {
            Examiner examiner = new Examiner();
            examiner.setUsername(username);
            examiner.setPassword(password);
            examiner.setName(name);
            examiner.setAvatar(avatar);
            examiner.setPhone(phone);
            examiner.setEmail(email);
            examiner.setExaminerNo(userNo);
            examiner.setRole("HELPER");
            examinerMapper.insert(examiner);
        } else if ("USER".equals(newSource)) {
            Student student = new Student();
            student.setUsername(username);
            student.setPassword(password);
            student.setName(name);
            student.setAvatar(avatar);
            student.setPhone(phone);
            student.setEmail(email);
            student.setStudentNo(userNo);
            student.setRole("USER");
            student.setBranch(branch);
            student.setClassName(className);
            student.setStatus(status != null ? status : "APPROVED");
            studentMapper.insert(student);
        } else {
            return Result.error("未知的目标身份");
        }

        return Result.success();
    }

    private String getOrDefault(Map<String, Object> form, String key, String fallback) {
        Object val = form.get(key);
        if (val != null && !val.toString().isEmpty()) return val.toString();
        return fallback;
    }

    /** 角色对应的权限等级 */
    private int getLevelBySource(String source) {
        if ("OWNER".equals(source)) return 4;
        if ("ADMIN".equals(source)) return 3;
        if ("HELPER".equals(source)) return 2;
        if ("USER".equals(source)) return 1;
        return 0;
    }

    /**
     * 统一用户列表（合并 admin、examiner、student）
     */
    @GetMapping("/selectAllUnified")
    public Result selectAllUnified(@RequestParam(required = false) String name,
                                   @RequestParam(required = false) String role) {
        List<UnifiedUser> list = new ArrayList<>();

        // 管理员
        for (Admin a : adminService.selectAll(new Admin())) {
            UnifiedUser u = new UnifiedUser();
            u.setId(a.getId());
            u.setUsername(a.getUsername());
            u.setName(a.getName());
            u.setAvatar(a.getAvatar());
            u.setPhone(a.getPhone());
            u.setEmail(a.getEmail());
            u.setUserNo(a.getAdminNo() != null ? a.getAdminNo() : "");
            u.setSource(a.getLevel() != null && a.getLevel() >= 4 ? "OWNER" : "ADMIN");
            u.setLevel(a.getLevel());
            u.setStatus("APPROVED");
            list.add(u);
        }

        // 阅卷人
        for (Examiner e : examinerService.selectAll(new Examiner())) {
            UnifiedUser u = new UnifiedUser();
            u.setId(e.getId());
            u.setUsername(e.getUsername());
            u.setName(e.getName());
            u.setAvatar(e.getAvatar());
            u.setPhone(e.getPhone());
            u.setEmail(e.getEmail());
            u.setUserNo(e.getExaminerNo() != null ? e.getExaminerNo() : "");
            u.setSource("HELPER");
            u.setStatus("APPROVED");
            list.add(u);
        }

        // 用户
        for (Student s : studentService.selectAll(new Student())) {
            UnifiedUser u = new UnifiedUser();
            u.setId(s.getId());
            u.setUsername(s.getUsername());
            u.setName(s.getName());
            u.setAvatar(s.getAvatar());
            u.setPhone(s.getPhone());
            u.setEmail(s.getEmail());
            u.setUserNo(s.getStudentNo() != null ? s.getStudentNo() : "");
            u.setClassName(s.getClassName() != null ? s.getClassName() : "");
            u.setBranch(s.getBranch() != null ? s.getBranch() : "");
            u.setStatus(s.getStatus() != null ? s.getStatus() : "APPROVED");
            u.setSource("USER");
            list.add(u);
        }

        // 按名称筛选
        if (name != null && !name.isEmpty()) {
            list = list.stream()
                    .filter(u -> u.getName() != null && u.getName().contains(name))
                    .collect(Collectors.toList());
        }
        // 按角色筛选
        if (role != null && !role.isEmpty()) {
            list = list.stream()
                    .filter(u -> role.equals(u.getSource()))
                    .collect(Collectors.toList());
        }

        // 按编号排序
        list.sort((a, b) -> {
            String noA = a.getUserNo();
            String noB = b.getUserNo();
            boolean emptyA = noA == null || noA.isEmpty();
            boolean emptyB = noB == null || noB.isEmpty();
            if (emptyA && emptyB) return 0;
            if (emptyA) return 1;
            if (emptyB) return -1;
            try {
                return Integer.compare(Integer.parseInt(noA), Integer.parseInt(noB));
            } catch (NumberFormatException ex) {
                return noA.compareTo(noB);
            }
        });

        return Result.success(list);
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Admin admin) {
        adminService.updateById(admin);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        adminService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        adminService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 单个查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Admin admin) {
        List<Admin> list = adminService.selectAll(admin);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Admin admin,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Admin> pageInfo = adminService.selectPage(admin, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
