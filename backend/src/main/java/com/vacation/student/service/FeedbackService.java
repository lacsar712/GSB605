package com.vacation.student.service;

import com.vacation.student.dto.FeedbackDTO;
import com.vacation.student.entity.Activity;
import com.vacation.student.entity.Feedback;
import com.vacation.student.entity.Registration;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.FeedbackMapper;
import com.vacation.student.mapper.RegistrationMapper;
import com.vacation.student.util.RichTextSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private FileService fileService;
    
    public Feedback findById(Long id) {
        return feedbackMapper.findById(id);
    }
    
    public List<Feedback> findByUserId(Long userId) {
        return feedbackMapper.findByUserId(userId);
    }
    
    public List<Feedback> findByActivityId(Long activityId) {
        return feedbackMapper.findByActivityId(activityId);
    }
    
    public List<Feedback> findByRegistrationId(Long registrationId, Long userId, String role) {
        Registration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            throw new IllegalArgumentException("报名记录不存在");
        }
        if ("STUDENT".equals(role) && !registration.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权查看该报名反馈");
        }
        return feedbackMapper.findByRegistrationId(registrationId);
    }
    
    public List<Feedback> findSubmittedByActivityId(Long activityId) {
        return feedbackMapper.findSubmittedByActivityId(activityId);
    }
    
    public Feedback create(Long userId, FeedbackDTO dto) {
        // 检查报名记录是否存在且已通过审批
        Registration registration = registrationMapper.findById(dto.getRegistrationId());
        if (registration == null) {
            throw new IllegalArgumentException("报名记录不存在");
        }
        
        if (!"APPROVED".equals(registration.getApprovalStatus())) {
            throw new IllegalArgumentException("报名审批未通过，无法提交反馈");
        }
        
        if (!registration.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此报名记录");
        }

        Activity activity = requireActivityAndValidateWindow(dto.getActivityId(), false);
        
        Feedback feedback = new Feedback();
        feedback.setActivityId(dto.getActivityId());
        feedback.setRegistrationId(dto.getRegistrationId());
        feedback.setUserId(userId);
        feedback.setTitle(dto.getTitle());
        feedback.setContent(RichTextSanitizer.sanitize(dto.getContent()));
        feedback.setAttachments(dto.getAttachments());
        feedback.setFeedbackType(dto.getFeedbackType() != null ? dto.getFeedbackType() : "SUMMARY");
        feedback.setStatus(1); // 直接提交
        
        feedbackMapper.insert(feedback);
        fileService.bindFiles(feedback.getAttachments(), "FEEDBACK", feedback.getId());
        return feedback;
    }
    
    public Feedback update(Long id, Long userId, FeedbackDTO dto) {
        Feedback feedback = feedbackMapper.findById(id);
        if (feedback == null) {
            throw new IllegalArgumentException("反馈记录不存在");
        }
        
        if (!feedback.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此反馈");
        }

        requireActivityAndValidateWindow(feedback.getActivityId(), true);
        
        feedback.setTitle(dto.getTitle());
        feedback.setContent(RichTextSanitizer.sanitize(dto.getContent()));
        feedback.setAttachments(dto.getAttachments());
        feedback.setFeedbackType(dto.getFeedbackType());
        
        feedbackMapper.update(feedback);
        fileService.bindFiles(feedback.getAttachments(), "FEEDBACK", feedback.getId());
        return feedback;
    }
    
    public void delete(Long id, Long userId) {
        Feedback feedback = feedbackMapper.findById(id);
        if (feedback == null) {
            throw new IllegalArgumentException("反馈记录不存在");
        }
        
        if (!feedback.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此反馈");
        }
        
        feedbackMapper.deleteById(id);
    }

    private Activity requireActivityAndValidateWindow(Long activityId, boolean isUpdate) {
        Activity activity = activityMapper.findById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (activity.getFeedbackStartTime() != null && now.isBefore(activity.getFeedbackStartTime())) {
            throw new IllegalArgumentException(isUpdate ? "反馈编辑时间未开始" : "反馈提交时间未开始");
        }
        if (activity.getFeedbackEndTime() != null && now.isAfter(activity.getFeedbackEndTime())) {
            throw new IllegalArgumentException(isUpdate ? "反馈编辑时间已截止" : "反馈提交时间已截止");
        }
        return activity;
    }
}
