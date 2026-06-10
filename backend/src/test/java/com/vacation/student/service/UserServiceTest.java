package com.vacation.student.service;

import com.vacation.student.dto.LoginDTO;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.UserMapper;
import com.vacation.student.util.JwtUtil;
import com.vacation.student.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void loginShouldReturnTokenAndUserWhenCredentialsValid() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));
        user.setRole("STUDENT");
        user.setStatus(1);

        when(userMapper.findByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mocked-token");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");

        Map<String, Object> result = userService.login(dto);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("mocked-token");
        assertThat(result).containsKey("user");
        assertThat(result.get("user")).isInstanceOf(User.class);
        User returnedUser = (User) result.get("user");
        assertThat(returnedUser.getPassword()).isNull();
        assertThat(returnedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("any");

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void loginShouldThrowWhenPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(PasswordUtil.simpleEncrypt("correct-password"));
        user.setStatus(1);

        when(userMapper.findByUsername("testuser")).thenReturn(user);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrong-password");

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("密码错误");
    }

    @Test
    void loginShouldThrowWhenAccountDisabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));
        user.setStatus(0);

        when(userMapper.findByUsername("testuser")).thenReturn(user);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("账号已被禁用");
    }

    @Test
    void loginShouldSanitizePasswordInReturnedUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("sanitized");
        user.setPassword(PasswordUtil.simpleEncrypt("mypass"));
        user.setRole("TEACHER");
        user.setStatus(1);

        when(userMapper.findByUsername("sanitized")).thenReturn(user);
        when(jwtUtil.generateToken(2L, "sanitized", "TEACHER")).thenReturn("token");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("sanitized");
        dto.setPassword("mypass");

        Map<String, Object> result = userService.login(dto);
        User returnedUser = (User) result.get("user");

        assertThat(returnedUser.getPassword()).isNull();
        assertThat(returnedUser.getId()).isEqualTo(2L);
        assertThat(returnedUser.getUsername()).isEqualTo("sanitized");
    }
}
