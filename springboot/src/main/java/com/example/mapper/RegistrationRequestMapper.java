package com.example.mapper;

import com.example.entity.RegistrationRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 注册申请 Mapper
 */
@Mapper
public interface RegistrationRequestMapper {

    /**
     * 插入注册申请
     */
    void insert(RegistrationRequest request);

    /**
     * 根据ID查询
     */
    RegistrationRequest selectById(@Param("id") Integer id);

    /**
     * 根据用户名查询
     */
    RegistrationRequest selectByUsername(@Param("username") String username);

    /**
     * 根据Token查询
     */
    RegistrationRequest selectByToken(@Param("token") String token);

    /**
     * 查询所有
     */
    List<RegistrationRequest> selectAll(RegistrationRequest request);

    /**
     * 更新申请状态
     */
    void updateStatus(@Param("id") Integer id,
                      @Param("status") String status,
                      @Param("approvalTime") LocalDateTime approvalTime,
                      @Param("approvedBy") Integer approvedBy,
                      @Param("rejectionReason") String rejectionReason);

    /**
     * 删除申请
     */
    void deleteById(@Param("id") Integer id);

    /**
     * 统计待审批数量
     */
    int countPending();
}
