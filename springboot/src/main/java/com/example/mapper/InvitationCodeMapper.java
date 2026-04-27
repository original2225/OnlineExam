package com.example.mapper;

import com.example.entity.InvitationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邀请码 Mapper
 */
@Mapper
public interface InvitationCodeMapper {

    /**
     * 插入邀请码
     */
    void insert(InvitationCode invitationCode);

    /**
     * 批量插入邀请码
     */
    void batchInsert(@Param("list") List<InvitationCode> list);

    /**
     * 根据ID查询
     */
    InvitationCode selectById(@Param("id") Integer id);

    /**
     * 根据邀请码查询
     */
    InvitationCode selectByCode(@Param("code") String code);

    /**
     * 查询所有
     */
    List<InvitationCode> selectAll(InvitationCode invitationCode);

    /**
     * 更新邀请码为已使用
     */
    void updateToUsed(@Param("id") Integer id,
                      @Param("usedBy") Integer usedBy,
                      @Param("usedByUsername") String usedByUsername,
                      @Param("usedTime") LocalDateTime usedTime);

    /**
     * 删除邀请码
     */
    void deleteById(@Param("id") Integer id);

    /**
     * 批���删除邀请码
     */
    void deleteBatch(@Param("ids") List<Integer> ids);

    /**
     * 删除所有未使用的邀请码
     */
    int deleteAllUnused();
}
