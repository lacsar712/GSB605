package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    /**
     * 用户角色: STUDENT-学生, TEACHER-教师, SCHOOL-学校管理员
     */
    private String role;
    /**
     * 学号/工号
     */
    private String studentNo;
    /**
     * 所属学校
     */
    private String school;
    /**
     * 所属院系
     */
    private String department;
    /**
     * 专业
     */
    private String major;
    /**
     * 年级
     */
    private String grade;
    /**
     * 绩点
     */
    private Double gpa;
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
