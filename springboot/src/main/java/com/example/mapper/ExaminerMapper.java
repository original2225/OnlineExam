package com.example.mapper;

import com.example.entity.Examiner;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExaminerMapper {

    int insert(Examiner examiner);

    void updateById(Examiner examiner);

    void deleteById(Integer id);

    @Select("select * from `examiner` where id = #{id}")
    Examiner selectById(Integer id);

    @Select("select * from `examiner` where username = #{username}")
    Examiner selectByUsername(String username);

    List<Examiner> selectAll(Examiner examiner);

}
