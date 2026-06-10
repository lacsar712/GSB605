package com.vacation.student.controller;

import com.vacation.student.dto.Result;
import com.vacation.student.entity.User;
import com.vacation.student.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestAttribute Long userId) {
        User user = userService.findById(userId);
        return Result.success(user);
    }
    
    @GetMapping("/{id}")
    public Result<User> getById(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        if (!"SCHOOL".equals(role) && !userId.equals(id)) {
            return Result.error(403, "无权限访问该用户");
        }
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
    
    @GetMapping
    public Result<List<User>> getAll(@RequestAttribute String role) {
        if (!"SCHOOL".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        List<User> users = userService.findAll();
        return Result.success(users);
    }
    
    @GetMapping("/role/{role}")
    public Result<List<User>> getByRole(@PathVariable String role, @RequestAttribute("role") String currentRole) {
        if (!"SCHOOL".equals(currentRole) && !"TEACHER".equals(currentRole)) {
            return Result.error(403, "无权限访问");
        }
        List<User> users = userService.findByRole(role);
        return Result.success(users);
    }
    
    @PutMapping("/me")
    public Result<User> updateCurrentUser(@RequestAttribute Long userId, @RequestBody User user) {
        user.setId(userId);
        User updated = userService.update(user);
        return Result.success("更新成功", updated);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestAttribute String role) {
        if (!"SCHOOL".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        userService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute String role) {
        if (!"SCHOOL".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        userService.delete(id);
        return Result.success("删除成功", null);
    }
}
