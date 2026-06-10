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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    void loginShouldThrowWhenUserNotFound() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("password");

        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void loginShouldThrowWhenPasswordIncorrect() {
        String rawPassword = "wrongpass";
        String correctPassword = "correctpass";

        User user = activeUser();
        user.setPassword(PasswordUtil.simpleEncrypt(correctPassword));

        LoginDTO dto = new LoginDTO();
        dto.setUsername("student");
        dto.setPassword(rawPassword);

        when(userMapper.findByUsername("student")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码错误");
    }

    @Test
    void loginShouldThrowWhenAccountDisabled() {
        String password = "password123";

        User user = activeUser();
        user.setStatus(0);
        user.setPassword(PasswordUtil.simpleEncrypt(password));

        LoginDTO dto = new LoginDTO();
        dto.setUsername("student");
        dto.setPassword(password);

        when(userMapper.findByUsername("student")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("账号已被禁用");
    }

    @Test
    void loginShouldReturnTokenAndSanitizedUserOnSuccess() {
        String password = "password123";
        String encryptedPassword = PasswordUtil.simpleEncrypt(password);
        String expectedToken = "jwt-token-abc123";

        User user = activeUser();
        user.setPassword(encryptedPassword);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("student");
        dto.setPassword(password);

        when(userMapper.findByUsername("student")).thenReturn(user);
        when(jwtUtil.generateToken(eq(1L), eq("student"), eq("STUDENT"))).thenReturn(expectedToken);

        Map<String, Object> result = userService.login(dto);

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo(expectedToken);
        assertThat(result).containsKey("user");

        User returnedUser = (User) result.get("user");
        assertThat(returnedUser.getId()).isEqualTo(1L);
        assertThat(returnedUser.getUsername()).isEqualTo("student");
        assertThat(returnedUser.getRole()).isEqualTo("STUDENT");
        assertThat(returnedUser.getPassword()).isNull();

        verify(jwtUtil).generateToken(1L, "student", "STUDENT");
    }

    private User activeUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("张三");
        user.setRole("STUDENT");
        user.setEmail("student@example.com");
        user.setStatus(1);
        return user;
    }
}
