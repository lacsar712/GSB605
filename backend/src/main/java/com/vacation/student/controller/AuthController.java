package com.vacation.student.controller;

import com.vacation.student.dto.LoginDTO;
import com.vacation.student.dto.Result;
import com.vacation.student.entity.User;
import com.vacation.student.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = userService.login(loginDTO);
        return Result.success("登录成功", result);
    }

    @PostMapping("/sso-login")
    public Result<Map<String, Object>> ssoLogin(@RequestBody Map<String, String> params) {
        String identity = params.get("identity");
        String password = params.get("password");
        if (identity == null || identity.isBlank() || password == null || password.isBlank()) {
            return Result.error("请提供统一身份标识和密码");
        }
        Map<String, Object> result = userService.ssoLogin(identity, password);
        return Result.success("统一身份认证成功", result);
    }
    
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User registered = userService.register(user);
        return Result.success("注册成功", registered);
    }
    
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestAttribute Long userId,
            @RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("请提供原密码和新密码");
        }
        
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String newPassword = params.get("newPassword");

        if (username == null || username.isBlank() || newPassword == null || newPassword.isBlank()) {
            return Result.error("请提供用户名和新密码");
        }

        userService.resetPassword(username, newPassword);
        return Result.success("密码重置成功，请使用新密码登录", null);
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> params) {
        String identity = params.get("identity");
        String email = params.get("email");
        String studentNo = params.get("studentNo");
        String newPassword = params.get("newPassword");

        if (identity == null || identity.isBlank() || email == null || email.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return Result.error("请完整填写统一身份标识、邮箱和新密码");
        }

        userService.resetPasswordByIdentity(identity, email, studentNo, newPassword);
        return Result.success("密码找回成功，请重新登录", null);
    }
}
