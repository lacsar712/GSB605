package com.vacation.student.controller;

import com.vacation.student.dto.FeedbackDTO;
import com.vacation.student.dto.Result;
import com.vacation.student.entity.Feedback;
import com.vacation.student.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedbacks(@RequestAttribute Long userId) {
        List<Feedback> feedbacks = feedbackService.findByUserId(userId);
        return Result.success(feedbacks);
    }
    
    @GetMapping("/{id}")
    public Result<Feedback> getById(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        Feedback feedback = feedbackService.findById(id);
        if (feedback == null) {
            return Result.error("反馈记录不存在");
        }
        if ("STUDENT".equals(role) && !feedback.getUserId().equals(userId)) {
            return Result.error(403, "无权限访问该反馈记录");
        }
        return Result.success(feedback);
    }
    
    @GetMapping("/activity/{activityId}")
    public Result<List<Feedback>> getByActivity(
            @PathVariable Long activityId,
            @RequestAttribute String role) {
        
        List<Feedback> feedbacks;
        if ("SCHOOL".equals(role) || "TEACHER".equals(role)) {
            feedbacks = feedbackService.findByActivityId(activityId);
        } else {
            feedbacks = feedbackService.findSubmittedByActivityId(activityId);
        }
        
        return Result.success(feedbacks);
    }
    
    @GetMapping("/registration/{registrationId}")
    public Result<List<Feedback>> getByRegistration(
            @PathVariable Long registrationId,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        List<Feedback> feedbacks = feedbackService.findByRegistrationId(registrationId, userId, role);
        return Result.success(feedbacks);
    }
    
    @PostMapping
    public Result<Feedback> create(
            @Valid @RequestBody FeedbackDTO dto,
            @RequestAttribute Long userId) {
        Feedback feedback = feedbackService.create(userId, dto);
        return Result.success("反馈提交成功", feedback);
    }
    
    @PutMapping("/{id}")
    public Result<Feedback> update(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDTO dto,
            @RequestAttribute Long userId) {
        Feedback feedback = feedbackService.update(id, userId, dto);
        return Result.success("反馈更新成功", feedback);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        feedbackService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
