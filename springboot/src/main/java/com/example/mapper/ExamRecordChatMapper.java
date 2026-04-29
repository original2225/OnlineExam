package com.example.mapper;

import com.example.entity.ExamRecordChatMessage;
import com.example.entity.ExamRecordChatRoom;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExamRecordChatMapper {
    int insertMessage(ExamRecordChatMessage message);

    List<ExamRecordChatMessage> selectMessages(@Param("recordId") Integer recordId,
                                                @Param("afterId") Integer afterId);

    void upsertRead(@Param("recordId") Integer recordId,
                    @Param("userId") Integer userId,
                    @Param("userRole") String userRole,
                    @Param("messageId") Integer messageId);

    int unreadCount(@Param("recordId") Integer recordId,
                    @Param("userId") Integer userId,
                    @Param("userRole") String userRole);

    List<ExamRecordChatRoom> selectRooms(@Param("userId") Integer userId,
                                         @Param("userRole") String userRole,
                                         @Param("isReviewer") boolean isReviewer);

    @Select("select max(id) from exam_record_chat_message where record_id = #{recordId} and is_deleted = 0")
    Integer selectLastMessageId(Integer recordId);
}
