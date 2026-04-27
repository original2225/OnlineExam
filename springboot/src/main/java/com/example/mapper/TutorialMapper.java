package com.example.mapper;

import com.example.entity.Tutorial;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TutorialMapper {

    int insert(Tutorial tutorial);

    void updateById(Tutorial tutorial);

    void deleteById(Integer id);

    @Select("select * from `tutorial` where id = #{id}")
    Tutorial selectById(Integer id);

    List<Tutorial> selectAll(Tutorial tutorial);

    @Update("update `tutorial` set view_count = view_count + 1 where id = #{id}")
    void incrementViewCount(Integer id);
}
