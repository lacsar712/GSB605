package com.vacation.student.mapper;

import com.vacation.student.entity.SchoolName;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SchoolNameMapper {
    
    @Select("SELECT * FROM school_names WHERE id = #{id}")
    SchoolName findById(Long id);
    
    @Select("SELECT * FROM school_names WHERE standard_name = #{name}")
    SchoolName findByStandardName(String name);
    
    @Select("SELECT * FROM school_names WHERE standard_name LIKE CONCAT('%',#{keyword},'%') " +
            "OR aliases LIKE CONCAT('%',#{keyword},'%') ORDER BY usage_count DESC LIMIT 10")
    List<SchoolName> searchByKeyword(String keyword);
    
    @Select("SELECT * FROM school_names ORDER BY usage_count DESC")
    List<SchoolName> findAll();
    
    @Insert("INSERT INTO school_names (standard_name, aliases, usage_count, create_time, update_time) " +
            "VALUES (#{standardName}, #{aliases}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SchoolName schoolName);
    
    @Update("UPDATE school_names SET aliases = #{aliases}, update_time = NOW() WHERE id = #{id}")
    int updateAliases(SchoolName schoolName);
    
    @Update("UPDATE school_names SET usage_count = usage_count + 1, update_time = NOW() WHERE id = #{id}")
    int incrementUsageCount(Long id);
    
    @Delete("DELETE FROM school_names WHERE id = #{id}")
    int deleteById(Long id);
}
