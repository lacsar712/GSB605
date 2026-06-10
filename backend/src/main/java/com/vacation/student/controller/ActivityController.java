package com.vacation.student.controller;

import com.vacation.student.dto.Result;
import com.vacation.student.entity.Activity;
import com.vacation.student.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @GetMapping
    public Result<List<Activity>> getActivities(
            @RequestAttribute Long userId,
            @RequestAttribute String role,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        
        List<Activity> activities;
        
        if ("STUDENT".equals(role)) {
            // 学生只能看到符合条件的活动
            if (type != null || keyword != null) {
                activities = activityService.search(type, keyword);
            } else {
                activities = activityService.findVisibleActivities(userId);
            }
        } else if ("TEACHER".equals(role)) {
            // 教师可以看到自己创建的活动
            activities = activityService.findByCreatorId(userId);
        } else {
            // 学校管理员可以看到所有活动
            activities = activityService.findAll();
        }
        
        return Result.success(activities);
    }
    
    @GetMapping("/published")
    public Result<List<Activity>> getPublishedActivities(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        List<Activity> activities = activityService.search(type, keyword);
        return Result.success(activities);
    }
    
    @GetMapping("/banners")
    public Result<List<Activity>> getBanners() {
        List<Activity> banners = activityService.findBanners();
        return Result.success(banners);
    }
    
    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Long id) {
        Activity activity = activityService.findById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }
    
    @PostMapping
    public Result<Activity> create(
            @RequestBody Activity activity,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        
        if (!"SCHOOL".equals(role) && !"TEACHER".equals(role)) {
            return Result.error(403, "无权限创建活动");
        }
        
        activity.setCreatorId(userId);
        Activity created = activityService.create(activity);
        return Result.success("活动创建成功", created);
    }
    
    @PutMapping("/{id}")
    public Result<Activity> update(
            @PathVariable Long id,
            @RequestBody Activity activity,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        
        Activity existing = activityService.findById(id);
        if (existing == null) {
            return Result.error("活动不存在");
        }
        
        // 检查权限
        if (!"SCHOOL".equals(role) && !existing.getCreatorId().equals(userId)) {
            return Result.error(403, "无权限修改该活动");
        }
        
        activity.setId(id);
        Activity updated = activityService.update(activity);
        return Result.success("活动更新成功", updated);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        
        Activity existing = activityService.findById(id);
        if (existing == null) {
            return Result.error("活动不存在");
        }
        
        // 检查权限
        if (!"SCHOOL".equals(role) && !existing.getCreatorId().equals(userId)) {
            return Result.error(403, "无权限操作");
        }
        
        activityService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        
        Activity existing = activityService.findById(id);
        if (existing == null) {
            return Result.error("活动不存在");
        }
        
        // 检查权限
        if (!"SCHOOL".equals(role) && !existing.getCreatorId().equals(userId)) {
            return Result.error(403, "无权限删除该活动");
        }
        
        activityService.delete(id);
        return Result.success("删除成功", null);
    }
}
