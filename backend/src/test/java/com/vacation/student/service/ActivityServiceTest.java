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
    void isEligibleShouldReturnTrueWhenAllConditionsAreSatisfied() {
        User user = baseUser();
        Activity activity = baseActivity();

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldRejectWhenGpaIsBelowRequirement() {
        User user = baseUser();
        user.setGpa(2.5);
        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldRejectWhenGpaIsNullButActivityRequiresGpa() {
        User user = baseUser();
        user.setGpa(null);
        Activity activity = baseActivity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldRejectWhenGradeNotInRequiredList() {
        User user = baseUser();
        user.setGrade("2021");
        Activity activity = baseActivity();
        activity.setRequiredGrades("2022,2023");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldAcceptWhenGradeMatchesOneOfRequiredGrades() {
        User user = baseUser();
        user.setGrade("2023");
        Activity activity = baseActivity();
        activity.setRequiredGrades("2022,2023");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldRejectWhenMajorNotInRequiredList() {
        User user = baseUser();
        user.setMajor("英语");
        Activity activity = baseActivity();
        activity.setRequiredMajors("计算机科学,软件工程");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldRejectWhenSignupHasNotStarted() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setSignupStartTime(LocalDateTime.now().plusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(5));

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldRejectWhenSignupHasEnded() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(5));
        activity.setSignupEndTime(LocalDateTime.now().minusDays(1));

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldRejectWhenParticipantsHasReachedMax() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(10);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldAcceptWhenMaxParticipantsIsZero() {
        User user = baseUser();
        Activity activity = baseActivity();
        activity.setMaxParticipants(0);
        activity.setCurrentParticipants(100);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    private User baseUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setGpa(3.5);
        user.setGrade("2023");
        user.setMajor("计算机科学");
        return user;
    }

    private Activity baseActivity() {
        Activity activity = new Activity();
        activity.setId(100L);
        activity.setTitle("暑期实践");
        activity.setRequiredGpa(3.0);
        activity.setRequiredGrades("2022,2023");
        activity.setRequiredMajors("计算机科学,软件工程");
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(5));
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(10);
        return activity;
    }
}
