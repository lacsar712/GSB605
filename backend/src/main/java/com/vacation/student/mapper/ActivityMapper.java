package com.vacation.student.mapper;

import com.vacation.student.entity.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityMapper {
    
    @Select("SELECT * FROM activities WHERE id = #{id}")
    Activity findById(Long id);
    
    @Select("SELECT * FROM activities WHERE status = 1 ORDER BY create_time DESC")
    List<Activity> findAllPublished();
    
    @Select("SELECT * FROM activities WHERE status = 1 AND activity_type = #{type} ORDER BY create_time DESC")
    List<Activity> findByType(String type);
    
    @Select("SELECT * FROM activities WHERE status = 1 AND is_banner = 1 ORDER BY create_time DESC")
    List<Activity> findBanners();
    
    @Select("SELECT * FROM activities WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<Activity> findByCreatorId(Long creatorId);
    
    @Select("SELECT * FROM activities ORDER BY create_time DESC")
    List<Activity> findAll();
    
    @Select("<script>" +
            "SELECT * FROM activities WHERE status = 1 " +
            "<if test='type != null'> AND activity_type = #{type}</if>" +
            "<if test='keyword != null'> AND (title LIKE CONCAT('%',#{keyword},'%') OR summary LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<Activity> search(@Param("type") String type, @Param("keyword") String keyword);
    
    @Insert("INSERT INTO activities (title, summary, content, cover_image, activity_type, signup_start_time, signup_end_time, " +
            "activity_start_time, activity_end_time, location, max_participants, current_participants, required_gpa, " +
            "required_grades, required_majors, need_approval, is_banner, banner_image, attachments, registration_config, " +
            "feedback_start_time, feedback_end_time, status, creator_id, create_time, update_time) " +
            "VALUES (#{title}, #{summary}, #{content}, #{coverImage}, #{activityType}, #{signupStartTime}, #{signupEndTime}, " +
            "#{activityStartTime}, #{activityEndTime}, #{location}, #{maxParticipants}, 0, #{requiredGpa}, " +
            "#{requiredGrades}, #{requiredMajors}, #{needApproval}, #{isBanner}, #{bannerImage}, #{attachments}, #{registrationConfig}, " +
            "#{feedbackStartTime}, #{feedbackEndTime}, #{status}, #{creatorId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);
    
    @Update("UPDATE activities SET title = #{title}, summary = #{summary}, content = #{content}, cover_image = #{coverImage}, " +
            "activity_type = #{activityType}, signup_start_time = #{signupStartTime}, signup_end_time = #{signupEndTime}, " +
            "activity_start_time = #{activityStartTime}, activity_end_time = #{activityEndTime}, location = #{location}, " +
            "max_participants = #{maxParticipants}, required_gpa = #{requiredGpa}, required_grades = #{requiredGrades}, " +
            "required_majors = #{requiredMajors}, need_approval = #{needApproval}, is_banner = #{isBanner}, " +
            "banner_image = #{bannerImage}, attachments = #{attachments}, registration_config = #{registrationConfig}, " +
            "feedback_start_time = #{feedbackStartTime}, feedback_end_time = #{feedbackEndTime}, update_time = NOW() WHERE id = #{id}")
    int update(Activity activity);
    
    @Update("UPDATE activities SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Update("UPDATE activities SET current_participants = current_participants + 1, update_time = NOW() WHERE id = #{id}")
    int incrementParticipants(Long id);
    
    @Delete("DELETE FROM activities WHERE id = #{id}")
    int deleteById(Long id);
}
