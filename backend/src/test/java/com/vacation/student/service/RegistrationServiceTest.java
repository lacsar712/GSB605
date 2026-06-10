package com.vacation.student.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacation.student.dto.RegistrationDTO;
import com.vacation.student.entity.Activity;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.ActivityGroupMapper;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.RegistrationMapper;
import com.vacation.student.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ActivityGroupMapper activityGroupMapper;

    @Mock
    private SchoolNameService schoolNameService;

    @Mock
    private ActivityService activityService;

    @Mock
    private FileService fileService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerShouldRejectWhenConfiguredRequiredFieldIsMissing() {
        Activity activity = new Activity();
        activity.setId(10L);
        activity.setRegistrationConfig("""
                [
                  {"key":"targetSchool","label":"招生学校","required":true},
                  {"key":"remark","label":"补充说明","required":true}
                ]
                """);

        User user = new User();
        user.setId(8L);

        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(10L);
        dto.setTargetSchool("示范大学");

        when(activityMapper.findById(10L)).thenReturn(activity);
        when(userMapper.findById(8L)).thenReturn(user);
        when(registrationMapper.findByActivityAndUser(10L, 8L)).thenReturn(null);
        when(activityService.isEligible(user, activity)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(8L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("补充说明");
    }
}
