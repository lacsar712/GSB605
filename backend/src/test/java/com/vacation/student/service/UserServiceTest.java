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
    void loginShouldReturnTokenAndSanitizedUserWhenCredentialsAreValid() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");

        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        user.setPassword(PasswordUtil.simpleEncrypt("plain-password"));
        user.setRole("STUDENT");
        user.setStatus(1);

        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(jwtUtil.generateToken(7L, "alice", "STUDENT")).thenReturn("mock-token");

        Map<String, Object> result = userService.login(dto);

        assertThat(result).containsEntry("token", "mock-token");
        User returnedUser = (User) result.get("user");
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser.getId()).isEqualTo(7L);
        assertThat(returnedUser.getUsername()).isEqualTo("alice");
        assertThat(returnedUser.getPassword()).isNull();
        verify(jwtUtil).generateToken(7L, "alice", "STUDENT");
    }

    @Test
    void loginShouldThrowWhenUserNotFound() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("ghost");
        dto.setPassword("anything");

        when(userMapper.findByUsername("ghost")).thenReturn(null);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void loginShouldThrowWhenPasswordIsIncorrect() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("wrong-password");

        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        user.setPassword(PasswordUtil.simpleEncrypt("right-password"));
        user.setRole("STUDENT");
        user.setStatus(1);

        when(userMapper.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("密码错误");
    }

    @Test
    void loginShouldThrowWhenAccountIsDisabled() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("alice");
        dto.setPassword("plain-password");

        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        user.setPassword(PasswordUtil.simpleEncrypt("plain-password"));
        user.setRole("STUDENT");
        user.setStatus(0);

        when(userMapper.findByUsername("alice")).thenReturn(user);

        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("账号已被禁用");
    }
}
