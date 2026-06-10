package com.vacation.student.controller;

import com.vacation.student.dto.RegistrationDTO;
import com.vacation.student.dto.Result;
import com.vacation.student.entity.Registration;
import com.vacation.student.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {
    
    @Autowired
    private RegistrationService registrationService;
    
    @GetMapping("/my")
    public Result<List<Registration>> getMyRegistrations(@RequestAttribute Long userId) {
        List<Registration> registrations = registrationService.findByUserId(userId);
        return Result.success(registrations);
    }
    
    @GetMapping("/{id}")
    public Result<Registration> getById(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        Registration registration = registrationService.findById(id);
        if (registration == null) {
            return Result.error("报名记录不存在");
        }
        if ("STUDENT".equals(role) && !registration.getUserId().equals(userId)) {
            return Result.error(403, "无权限访问该报名记录");
        }
        return Result.success(registration);
    }
    
    @GetMapping("/activity/{activityId}")
    public Result<List<Registration>> getByActivity(
            @PathVariable Long activityId,
            @RequestAttribute String role) {
        
        if (!"SCHOOL".equals(role) && !"TEACHER".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        List<Registration> registrations = registrationService.findByActivityId(activityId);
        return Result.success(registrations);
    }
    
    @GetMapping("/activity/{activityId}/status/{status}")
    public Result<List<Registration>> getByActivityAndStatus(
            @PathVariable Long activityId,
            @PathVariable String status,
            @RequestAttribute String role) {
        
        if (!"SCHOOL".equals(role) && !"TEACHER".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        List<Registration> registrations = registrationService.findByActivityIdAndStatus(activityId, status);
        return Result.success(registrations);
    }
    
    @GetMapping("/group/{groupId}")
    public Result<List<Registration>> getByGroup(@PathVariable Long groupId) {
        List<Registration> registrations = registrationService.findByGroupId(groupId);
        return Result.success(registrations);
    }
    
    @GetMapping("/check/{activityId}")
    public Result<Boolean> checkRegistration(
            @PathVariable Long activityId,
            @RequestAttribute Long userId) {
        Registration existing = registrationService.findByActivityAndUser(activityId, userId);
        return Result.success(existing != null);
    }
    
    @PostMapping
    public Result<Registration> register(
            @Valid @RequestBody RegistrationDTO dto,
            @RequestAttribute Long userId) {
        Registration registration = registrationService.register(userId, dto);
        return Result.success("报名成功", registration);
    }
    
    @PutMapping("/{id}/approve")
    public Result<Registration> approve(
            @PathVariable Long id,
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        
        if (!"SCHOOL".equals(role) && !"TEACHER".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        
        String status = params.get("status");
        String comment = params.get("comment");
        
        if (status == null || (!status.equals("APPROVED") && !status.equals("REJECTED"))) {
            return Result.error("状态值无效");
        }
        
        Registration registration = registrationService.approve(id, userId, status, comment);
        return Result.success("审批完成", registration);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> cancel(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        
        Registration registration = registrationService.findById(id);
        if (registration == null) {
            return Result.error("报名记录不存在");
        }
        
        if (!registration.getUserId().equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        if (!"PENDING".equals(registration.getApprovalStatus())) {
            return Result.error("只能取消待审批的报名");
        }
        
        registrationService.delete(id);
        return Result.success("取消报名成功", null);
    }
}
