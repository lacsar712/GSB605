package com.vacation.student.service;

import com.vacation.student.entity.Activity;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileService fileService;
    
    public Activity findById(Long id) {
        return activityMapper.findById(id);
    }
    
    public List<Activity> findAllPublished() {
        return activityMapper.findAllPublished();
    }
    
    public List<Activity> findByType(String type) {
        return activityMapper.findByType(type);
    }
    
    public List<Activity> findBanners() {
        return activityMapper.findBanners();
    }
    
    public List<Activity> findByCreatorId(Long creatorId) {
        return activityMapper.findByCreatorId(creatorId);
    }
    
    public List<Activity> findAll() {
        return activityMapper.findAll();
    }
    
    public List<Activity> search(String type, String keyword) {
        return activityMapper.search(type, keyword);
    }
    
    /**
     * 获取学生可见的活动列表（根据资格条件过滤）
     */
    public List<Activity> findVisibleActivities(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return List.of();
        }
        
        List<Activity> activities = activityMapper.findAllPublished();
        
        return activities.stream()
                .filter(activity -> isEligible(user, activity))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查学生是否有资格参加活动
     */
    public boolean isEligible(User user, Activity activity) {
        // 检查绩点要求
        if (activity.getRequiredGpa() != null && activity.getRequiredGpa() > 0) {
            if (user.getGpa() == null || user.getGpa() < activity.getRequiredGpa()) {
                return false;
            }
        }
        
        // 检查年级要求
        if (activity.getRequiredGrades() != null && !activity.getRequiredGrades().isEmpty()) {
            List<String> requiredGrades = Arrays.asList(activity.getRequiredGrades().split(","));
            if (user.getGrade() == null || !requiredGrades.contains(user.getGrade())) {
                return false;
            }
        }
        
        // 检查专业要求
        if (activity.getRequiredMajors() != null && !activity.getRequiredMajors().isEmpty()) {
            List<String> requiredMajors = Arrays.asList(activity.getRequiredMajors().split(","));
            if (user.getMajor() == null || !requiredMajors.contains(user.getMajor())) {
                return false;
            }
        }
        
        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (activity.getSignupStartTime() != null && now.isBefore(activity.getSignupStartTime())) {
            return false;
        }
        if (activity.getSignupEndTime() != null && now.isAfter(activity.getSignupEndTime())) {
            return false;
        }
        
        // 检查人数限制
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                return false;
            }
        }
        
        return true;
    }
    
    public Activity create(Activity activity) {
        activity.setCurrentParticipants(0);
        activityMapper.insert(activity);
        fileService.bindFiles(activity.getAttachments(), "ACTIVITY", activity.getId());
        return activity;
    }
    
    public Activity update(Activity activity) {
        activityMapper.update(activity);
        fileService.bindFiles(activity.getAttachments(), "ACTIVITY", activity.getId());
        return activityMapper.findById(activity.getId());
    }
    
    public void updateStatus(Long id, Integer status) {
        activityMapper.updateStatus(id, status);
    }
    
    public void incrementParticipants(Long id) {
        activityMapper.incrementParticipants(id);
    }
    
    public void delete(Long id) {
        activityMapper.deleteById(id);
    }
}
