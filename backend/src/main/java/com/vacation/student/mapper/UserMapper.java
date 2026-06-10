package com.vacation.student.mapper;

import com.vacation.student.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);
    
    @Select("SELECT * FROM users WHERE student_no = #{studentNo}")
    User findByStudentNo(String studentNo);
    
    @Select("SELECT * FROM users WHERE role = #{role} ORDER BY create_time DESC")
    List<User> findByRole(String role);
    
    @Select("SELECT * FROM users ORDER BY create_time DESC")
    List<User> findAll();
    
    @Insert("INSERT INTO users (username, password, real_name, email, phone, avatar, role, student_no, school, department, major, grade, gpa, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{role}, #{studentNo}, #{school}, #{department}, #{major}, #{grade}, #{gpa}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE users SET real_name = #{realName}, email = #{email}, phone = #{phone}, avatar = #{avatar}, " +
            "school = #{school}, department = #{department}, major = #{major}, grade = #{grade}, gpa = #{gpa}, update_time = NOW() WHERE id = #{id}")
    int update(User user);
    
    @Update("UPDATE users SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Update("UPDATE users SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);
}
