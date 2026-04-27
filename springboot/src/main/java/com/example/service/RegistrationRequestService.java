package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Exam;
import com.example.entity.ExamPermission;
import com.example.entity.Examiner;
import com.example.entity.InvitationCode;
import com.example.entity.RegistrationRequest;
import com.example.entity.Student;
import com.example.exception.CustomException;
import com.example.mapper.ExamMapper;
import com.example.mapper.ExamPermissionMapper;
import com.example.mapper.RegistrationRequestMapper;
import com.example.mapper.StudentMapper;
import com.example.utils.PasswordUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 注册申请服务
 */
@Service
public class RegistrationRequestService {

    @Resource
    private RegistrationRequestMapper registrationRequestMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private com.example.mapper.ExaminerMapper examinerMapper;

    @Resource
    private com.example.mapper.InvitationCodeMapper invitationCodeMapper;

    @Resource
    private InvitationCodeService invitationCodeService;

    @Resource
    private ExamMapper examMapper;

    @Resource
    private ExamPermissionMapper examPermissionMapper;

    /**
     * 玩家提交注册申请（支持邀请码直接通过）
     */
    @Transactional
    public Object submitRequest(RegistrationRequest request) {
        // 检查用户名是否已存在
        Student existStudent = studentMapper.selectByUsername(request.getUsername());
        if (ObjectUtil.isNotNull(existStudent)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }

        // 检查是否已有待审批的申请
        RegistrationRequest existRequest = registrationRequestMapper.selectByUsername(request.getUsername());
        if (ObjectUtil.isNotNull(existRequest) && "PENDING".equals(existRequest.getStatus())) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR, "您已有待审批的申请，请勿重复提交");
        }

        // 设置默认密码
        if (ObjectUtil.isEmpty(request.getPassword())) {
            request.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (PasswordUtils.needsUpgrade(request.getPassword())) {
            request.setPassword(PasswordUtils.encode(request.getPassword()));
        }

        // 设置默认姓名
        if (ObjectUtil.isEmpty(request.getName())) {
            request.setName(request.getUsername());
        }

        // 检查是否有邀请码
        String invitationCodeStr = request.getApprovalToken(); // 借用 approvalToken 字段传递邀请码

        if (ObjectUtil.isNotEmpty(invitationCodeStr)) {
            // 有邀请码，验证并直接创建账号
            try {
                InvitationCode invitationCode = invitationCodeService.validateAndUse(
                        invitationCodeStr,
                        null, // 此时用户还未创建，ID为空
                        request.getUsername()
                );

                // 根据邀请码的目标角色创建对应账号
                String targetRole = invitationCode.getTargetRole();
                if ("HELPER".equals(targetRole)) {
                    // 创建阅卷人账号
                    Examiner examiner = new Examiner();
                    examiner.setUsername(request.getUsername());
                    examiner.setPassword(request.getPassword());
                    examiner.setName(request.getName());
                    examiner.setEmail(request.getEmail());
                    examiner.setPhone(request.getPhone());
                    examiner.setRole(RoleEnum.HELPER.name());
                    examinerMapper.insert(examiner);

                    invitationCodeMapper.updateToUsed(
                            invitationCode.getId(),
                            examiner.getId(),
                            examiner.getUsername(),
                            LocalDateTime.now()
                    );
                    return examiner;
                } else {
                    // 默认创建玩家账号
                    Student student = new Student();
                    student.setUsername(request.getUsername());
                    student.setPassword(request.getPassword());
                    student.setName(request.getName());
                    student.setStudentNo(request.getStudentNo());
                    student.setEmail(request.getEmail());
                    student.setPhone(request.getPhone());
                    student.setClassName(request.getClassName());
                    student.setBranch(request.getBranch() != null ? request.getBranch() : "后勤见习");
                    student.setRole(RoleEnum.USER.name());
                    student.setStatus("APPROVED");
                    studentMapper.insert(student);

                    invitationCodeMapper.updateToUsed(
                            invitationCode.getId(),
                            student.getId(),
                            student.getUsername(),
                            LocalDateTime.now()
                    );
                    return student;
                }
            } catch (Exception e) {
                throw new CustomException(ResultCodeEnum.PARAM_ERROR, "邀请码无效：" + e.getMessage());
            }
        } else {
            // 无邀请码，创建待审批申请
            request.setStatus("PENDING");
            request.setRequestTime(LocalDateTime.now());
            request.setApprovalToken(null); // 清空邀请码字段，生成审批token
            request.setApprovalToken(generateToken());

            registrationRequestMapper.insert(request);
            return null; // 返回null表示需要等待审批
        }
    }

    /**
     * 管理员审批通过
     */
    @Transactional
    public void approveRequest(Integer id, Integer adminId) {
        RegistrationRequest request = registrationRequestMapper.selectById(id);
        if (ObjectUtil.isNull(request)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "申请不存在");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "该申请已被处理");
        }

        // 再次检查用户名是否已存在
        Student existStudent = studentMapper.selectByUsername(request.getUsername());
        if (ObjectUtil.isNotNull(existStudent)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR, "用户名已存在，无法创建账号");
        }

        // 创建玩家账号
        Student student = new Student();
        student.setUsername(request.getUsername());
        student.setPassword(request.getPassword());
        student.setName(request.getName());
        student.setStudentNo(request.getStudentNo());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setClassName(request.getClassName());
        student.setBranch(request.getBranch() != null ? request.getBranch() : "后勤见习");
        student.setRole(RoleEnum.USER.name());
        student.setStatus("APPROVED"); // 审批通过，账号状态为审核通过
        studentMapper.insert(student);

        // 根据玩家的分支，自动开通对应分支的考试权限
        grantExamPermissionsByBranch(student.getId(), student.getBranch());

        // 更新申请状态
        registrationRequestMapper.updateStatus(
                request.getId(),
                "APPROVED",
                LocalDateTime.now(),
                adminId,
                null
        );
    }

    /**
     * 管理员拒绝申请
     */
    @Transactional
    public void rejectRequest(Integer id, Integer adminId, String reason) {
        RegistrationRequest request = registrationRequestMapper.selectById(id);
        if (ObjectUtil.isNull(request)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "申请不存在");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR, "该申请已被处理");
        }

        // 更新申请状态
        registrationRequestMapper.updateStatus(
                request.getId(),
                "REJECTED",
                LocalDateTime.now(),
                adminId,
                reason
        );
    }

    /**
     * 查询所有申请
     */
    public List<RegistrationRequest> selectAll(RegistrationRequest request) {
        return registrationRequestMapper.selectAll(request);
    }

    /**
     * 分页查询申请
     */
    public PageInfo<RegistrationRequest> selectPage(RegistrationRequest request, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RegistrationRequest> list = registrationRequestMapper.selectAll(request);
        return PageInfo.of(list);
    }

    /**
     * 根据ID查询
     */
    public RegistrationRequest selectById(Integer id) {
        return registrationRequestMapper.selectById(id);
    }

    /**
     * 删除申请记录
     */
    public void deleteById(Integer id) {
        registrationRequestMapper.deleteById(id);
    }

    /**
     * 获取待审批数量
     */
    public int countPending() {
        return registrationRequestMapper.countPending();
    }

    /**
     * 根据玩家的分支，自动开通对应分支的考试权限
     */
    private void grantExamPermissionsByBranch(Integer studentId, String branch) {
        if (branch == null || branch.isEmpty()) {
            branch = "后勤见习";
        }

        // 查询该分支的所有已发布考试
        Exam examQuery = new Exam();
        examQuery.setBranch(branch);
        examQuery.setStatus("published");
        List<Exam> exams = examMapper.selectAll(examQuery);

        if (exams != null && !exams.isEmpty()) {
            List<ExamPermission> permissions = new ArrayList<>();
            for (Exam exam : exams) {
                ExamPermission permission = new ExamPermission();
                permission.setExamId(exam.getId());
                permission.setStudentId(studentId);
                permissions.add(permission);
            }
            // 批量插入考试权限
            if (!permissions.isEmpty()) {
                examPermissionMapper.batchInsert(permissions);
            }
        }
    }

    /**
     * 生成随机Token
     */
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "") + RandomUtil.randomString(16);
    }
}
