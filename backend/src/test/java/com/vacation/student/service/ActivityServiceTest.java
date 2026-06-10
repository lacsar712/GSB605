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
    void isEligibleShouldReturnTrueWhenAllConditionsMet() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenNoRestrictions() {
        User user = new User();
        user.setId(1L);
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setCurrentParticipants(0);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGpaBelowRequirement() {
        User user = createEligibleUser();
        user.setGpa(2.5);
        Activity activity = createOpenActivity();
        activity.setRequiredGpa(3.0);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenGpaMeetsRequirement() {
        User user = createEligibleUser();
        user.setGpa(3.5);
        Activity activity = createOpenActivity();
        activity.setRequiredGpa(3.0);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGpaIsNullAndRequired() {
        User user = createEligibleUser();
        user.setGpa(null);
        Activity activity = createOpenActivity();
        activity.setRequiredGpa(3.0);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGradeNotInRequiredList() {
        User user = createEligibleUser();
        user.setGrade("大三");
        Activity activity = createOpenActivity();
        activity.setRequiredGrades("大一,大二");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenGradeInRequiredList() {
        User user = createEligibleUser();
        user.setGrade("大二");
        Activity activity = createOpenActivity();
        activity.setRequiredGrades("大一,大二");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGradeIsNullAndRequired() {
        User user = createEligibleUser();
        user.setGrade(null);
        Activity activity = createOpenActivity();
        activity.setRequiredGrades("大一,大二");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMajorNotInRequiredList() {
        User user = createEligibleUser();
        user.setMajor("物理学");
        Activity activity = createOpenActivity();
        activity.setRequiredMajors("计算机科学,软件工程");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenMajorInRequiredList() {
        User user = createEligibleUser();
        user.setMajor("计算机科学");
        Activity activity = createOpenActivity();
        activity.setRequiredMajors("计算机科学,软件工程");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserMajorIsNullAndRequired() {
        User user = createEligibleUser();
        user.setMajor(null);
        Activity activity = createOpenActivity();
        activity.setRequiredMajors("计算机科学,软件工程");

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseBeforeSignupStartTime() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();
        activity.setSignupStartTime(LocalDateTime.now().plusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseAfterSignupEndTime() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(10));
        activity.setSignupEndTime(LocalDateTime.now().minusDays(1));

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueDuringSignupPeriod() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMaxParticipantsReached() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(50);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenSlotsAvailable() {
        User user = createEligibleUser();
        Activity activity = createOpenActivity();
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(30);

        boolean result = activityService.isEligible(user, activity);

        assertThat(result).isTrue();
    }

    private User createEligibleUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setGpa(3.8);
        user.setGrade("大二");
        user.setMajor("计算机科学");
        return user;
    }

    private Activity createOpenActivity() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(10));
        activity.setCurrentParticipants(10);
        return activity;
    }
}
