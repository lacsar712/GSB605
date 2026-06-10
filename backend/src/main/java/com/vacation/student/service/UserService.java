package com.vacation.student.service;

import com.vacation.student.dto.LoginDTO;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.UserMapper;
import com.vacation.student.util.JwtUtil;
import com.vacation.student.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.findByUsername(loginDTO.getUsername());
        return buildLoginResult(user, loginDTO.getPassword());
    }

    public Map<String, Object> ssoLogin(String identity, String password) {
        User user = findRawByIdentity(identity);
        return buildLoginResult(user, password);
    }

    private Map<String, Object> buildLoginResult(User user, String password) {
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        String encryptedPassword = PasswordUtil.simpleEncrypt(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        return result;
    }
    
    public User register(User user) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(PasswordUtil.simpleEncrypt(user.getPassword()));
        user.setStatus(1);
        
        userMapper.insert(user);
        return sanitizeUser(user);
    }
    
    public User findById(Long id) {
        User user = userMapper.findById(id);
        return user != null ? sanitizeUser(user) : null;
    }
    
    public User findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return user != null ? sanitizeUser(user) : null;
    }
    
    public List<User> findByRole(String role) {
        List<User> users = userMapper.findByRole(role);
        users.forEach(this::sanitizeUser);
        return users;
    }
    
    public List<User> findAll() {
        List<User> users = userMapper.findAll();
        users.forEach(this::sanitizeUser);
        return users;
    }
    
    public User update(User user) {
        userMapper.update(user);
        return findById(user.getId());
    }
    
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        String encryptedOldPassword = PasswordUtil.simpleEncrypt(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        
        String encryptedNewPassword = PasswordUtil.simpleEncrypt(newPassword);
        userMapper.updatePassword(id, encryptedNewPassword);
    }

    public void resetPassword(String username, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度至少6位");
        }

        String encryptedNewPassword = PasswordUtil.simpleEncrypt(newPassword);
        userMapper.updatePassword(user.getId(), encryptedNewPassword);
    }

    public void resetPasswordByIdentity(String identity, String email, String studentNo, String newPassword) {
        User user = findRawByIdentity(identity);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (email == null || email.isBlank() || !email.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("身份校验失败，请确认邮箱信息");
        }
        if (studentNo != null && !studentNo.isBlank()) {
            String currentStudentNo = user.getStudentNo();
            if (currentStudentNo == null || !currentStudentNo.equals(studentNo)) {
                throw new IllegalArgumentException("身份校验失败，请确认学号/工号信息");
            }
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度至少6位");
        }
        userMapper.updatePassword(user.getId(), PasswordUtil.simpleEncrypt(newPassword));
    }
    
    public void updateStatus(Long id, Integer status) {
        userMapper.updateStatus(id, status);
    }
    
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
    
    private User sanitizeUser(User user) {
        user.setPassword(null);
        return user;
    }

    private User findRawByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            return null;
        }
        User user = userMapper.findByUsername(identity);
        if (user != null) {
            return user;
        }
        user = userMapper.findByStudentNo(identity);
        if (user != null) {
            return user;
        }
        return userMapper.findByEmail(identity);
    }
}
