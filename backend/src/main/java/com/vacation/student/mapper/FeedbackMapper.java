package com.vacation.student.mapper;

import com.vacation.student.entity.Feedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackMapper {
    
    @Select("SELECT * FROM feedbacks WHERE id = #{id}")
    Feedback findById(Long id);
    
    @Select("SELECT * FROM feedbacks WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Feedback> findByUserId(Long userId);
    
    @Select("SELECT * FROM feedbacks WHERE activity_id = #{activityId} ORDER BY create_time DESC")
    List<Feedback> findByActivityId(Long activityId);
    
    @Select("SELECT * FROM feedbacks WHERE registration_id = #{registrationId} ORDER BY create_time DESC")
    List<Feedback> findByRegistrationId(Long registrationId);
    
    @Select("SELECT * FROM feedbacks WHERE activity_id = #{activityId} AND status = 1 ORDER BY create_time DESC")
    List<Feedback> findSubmittedByActivityId(Long activityId);
    
    @Insert("INSERT INTO feedbacks (activity_id, registration_id, user_id, title, content, attachments, feedback_type, status, create_time, update_time) " +
            "VALUES (#{activityId}, #{registrationId}, #{userId}, #{title}, #{content}, #{attachments}, #{feedbackType}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);
    
    @Update("UPDATE feedbacks SET title = #{title}, content = #{content}, attachments = #{attachments}, " +
            "feedback_type = #{feedbackType}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(Feedback feedback);
    
    @Delete("DELETE FROM feedbacks WHERE id = #{id}")
    int deleteById(Long id);
}
