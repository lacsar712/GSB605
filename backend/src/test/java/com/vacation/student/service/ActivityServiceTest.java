package com.vacation.student.service;

import com.vacation.student.entity.Activity;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void isEligibleShouldReturnTrueWhenNoRequirements() {
        User user = baseUser();
        Activity activity = baseActivity();

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGpaNotMet() {
        User user = baseUser();
        user.setGpa(2.5);
        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGpaIsNull() {
        User user = baseUser();
        user.setGpa(null);
        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenGpaMeetsRequirement() {
        User user = baseUser();
        user.setGpa(3.5);
        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGradeNotInRequired() {
        User user = baseUser();
        user.setGrade("2022");
        Activity activity = baseActivity();
        activity.setRequiredGrades("2023,2024");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGradeIsNull() {
        User user = baseUser();
        user.setGrade(null);
        Activity activity = baseActivity();
        activity.setRequiredGrades("2023,2024");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenGradeInRequired() {
        User user = baseUser();
        user.setGrade("2023");
        Activity activity = baseActivity();
        activity.setRequiredGrades("2023,2024");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMajorNotInRequired() {
        User user = baseUser();
        user.setMajor("数学");
        Activity activity = baseActivity();
        activity.setRequiredMajors("计算机,软件工程");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserMajorIsNull() {
        User user = baseUser();
        user.setMajor(null);
        Activity activity = baseActivity();
        activity.setRequiredMajors("计算机,软件工程");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenMajorInRequired() {
        User user = baseUser();
        user.setMajor("计算机");
        Activity activity = baseActivity();
        activity.setRequiredMajors("计算机,软件工程");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseBeforeSignupStarts() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setSignupStartTime(LocalDateTime.now().plusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseAfterSignupEnds() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(10));
        activity.setSignupEndTime(LocalDateTime.now().minusDays(1));

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueDuringSignupPeriod() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMaxParticipantsReached() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(10);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenPlacesAvailable() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(5);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldPassAllCombinedRequirements() {
        User user = baseUser();
        user.setGpa(3.8);
        user.setGrade("2023");
        user.setMajor("计算机");

        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);
        activity.setRequiredGrades("2022,2023,2024");
        activity.setRequiredMajors("计算机,软件工程");
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));
        activity.setMaxParticipants(20);
        activity.setCurrentParticipants(5);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    private User baseUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setRole("STUDENT");
        user.setGpa(3.0);
        user.setGrade("2023");
        user.setMajor("计算机");
        user.setStatus(1);
        return user;
    }

    private Activity baseActivity() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setCurrentParticipants(0);
        return activity;
    }
}
