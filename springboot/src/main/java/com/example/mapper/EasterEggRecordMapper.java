package com.example.mapper;

import com.example.entity.EasterEggRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EasterEggRecordMapper {

    int insert(EasterEggRecord record);

    EasterEggRecord selectByUserAndRoleAndEgg(@Param("userId") Integer userId,
                                               @Param("userRole") String userRole,
                                               @Param("eggName") String eggName);

    List<EasterEggRecord> selectLeaderboard();

    List<EasterEggRecord> selectByEggName(@Param("eggName") String eggName);

    int selectCountByEggName(@Param("eggName") String eggName);

    List<EasterEggRecord> selectAllEggsByUser(@Param("userId") Integer userId,
                                               @Param("userRole") String userRole);
}
