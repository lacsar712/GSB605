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
    void loginShouldReturnTokenAndUserWhenCredentialsValid() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        User user = createActiveUser();
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));

        when(userMapper.findByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mock-jwt-token");

        Map<String, Object> result = userService.login(loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.get("token")).isEqualTo("mock-jwt-token");
        assertThat(result.get("user")).isInstanceOf(User.class);

        User resultUser = (User) result.get("user");
        assertThat(resultUser.getId()).isEqualTo(1L);
        assertThat(resultUser.getUsername()).isEqualTo("testuser");
        assertThat(resultUser.getPassword()).isNull();
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("password123");

        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        User user = createActiveUser();
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));

        when(userMapper.findByUsername("testuser")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码错误");
    }

    @Test
    void loginShouldThrowExceptionWhenUserDisabled() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("disableduser");
        loginDTO.setPassword("password123");

        User user = createActiveUser();
        user.setStatus(0);
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));

        when(userMapper.findByUsername("disableduser")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(loginDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("账号已被禁用");
    }

    @Test
    void loginShouldSanitizePasswordInResult() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        User user = createActiveUser();
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));

        when(userMapper.findByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mock-jwt-token");

        Map<String, Object> result = userService.login(loginDTO);
        User resultUser = (User) result.get("user");

        assertThat(resultUser.getPassword()).isNull();
    }

    @Test
    void loginShouldCallJwtUtilWithCorrectParameters() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        User user = createActiveUser();
        user.setPassword(PasswordUtil.simpleEncrypt("password123"));

        when(userMapper.findByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mock-jwt-token");

        userService.login(loginDTO);

        verify(jwtUtil).generateToken(1L, "testuser", "STUDENT");
    }

    private User createActiveUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole("STUDENT");
        user.setStatus(1);
        return user;
    }
}
