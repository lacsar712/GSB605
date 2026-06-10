package com.vacation.student.mapper;

import com.vacation.student.entity.Registration;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RegistrationMapper {
    
    @Select("SELECT * FROM registrations WHERE id = #{id}")
    Registration findById(Long id);
    
    @Select("SELECT * FROM registrations WHERE activity_id = #{activityId} AND user_id = #{userId}")
    Registration findByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);
    
    @Select("SELECT * FROM registrations WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Registration> findByUserId(Long userId);
    
    @Select("SELECT * FROM registrations WHERE activity_id = #{activityId} ORDER BY create_time DESC")
    List<Registration> findByActivityId(Long activityId);
    
    @Select("SELECT * FROM registrations WHERE activity_id = #{activityId} AND approval_status = #{status} ORDER BY create_time DESC")
    List<Registration> findByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);
    
    @Select("SELECT * FROM registrations WHERE group_id = #{groupId} ORDER BY create_time DESC")
    List<Registration> findByGroupId(Long groupId);
    
    @Insert("INSERT INTO registrations (activity_id, user_id, target_school, contact_info, remark, attachments, " +
            "approval_status, group_id, create_time, update_time) " +
            "VALUES (#{activityId}, #{userId}, #{targetSchool}, #{contactInfo}, #{remark}, #{attachments}, " +
            "'PENDING', #{groupId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Registration registration);
    
    @Update("UPDATE registrations SET approval_status = #{approvalStatus}, approver_id = #{approverId}, " +
            "approval_time = NOW(), approval_comment = #{approvalComment}, update_time = NOW() WHERE id = #{id}")
    int updateApproval(Registration registration);
    
    @Update("UPDATE registrations SET group_id = #{groupId}, update_time = NOW() WHERE id = #{id}")
    int updateGroup(@Param("id") Long id, @Param("groupId") Long groupId);
    
    @Update("UPDATE registrations SET attachments = #{attachments}, update_time = NOW() WHERE id = #{id}")
    int updateAttachments(@Param("id") Long id, @Param("attachments") String attachments);
    
    @Delete("DELETE FROM registrations WHERE id = #{id}")
    int deleteById(Long id);
}
