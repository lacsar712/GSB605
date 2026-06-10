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
    void isEligibleShouldReturnTrueWhenNoRequirementsSet() {
        User user = new User();
        Activity activity = new Activity();

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenAllRequirementsMet() {
        User user = new User();
        user.setGpa(3.8);
        user.setGrade("大二");
        user.setMajor("计算机科学");

        Activity activity = new Activity();
        activity.setRequiredGpa(3.5);
        activity.setRequiredGrades("大一,大二,大三");
        activity.setRequiredMajors("计算机科学,软件工程");
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(30);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGpaBelowRequired() {
        User user = new User();
        user.setGpa(2.5);

        Activity activity = new Activity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGpaIsNullButRequired() {
        User user = new User();

        Activity activity = new Activity();
        activity.setRequiredGpa(3.0);

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenRequiredGpaIsZero() {
        User user = new User();
        user.setGpa(2.0);

        Activity activity = new Activity();
        activity.setRequiredGpa(0.0);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenRequiredGpaIsNull() {
        User user = new User();

        Activity activity = new Activity();
        activity.setRequiredGpa(null);

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenGradeNotInRequiredList() {
        User user = new User();
        user.setGrade("大四");

        Activity activity = new Activity();
        activity.setRequiredGrades("大一,大二,大三");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserGradeIsNullButRequired() {
        User user = new User();

        Activity activity = new Activity();
        activity.setRequiredGrades("大一,大二");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenRequiredGradesIsEmpty() {
        User user = new User();
        user.setGrade("大二");

        Activity activity = new Activity();
        activity.setRequiredGrades("");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMajorNotInRequiredList() {
        User user = new User();
        user.setMajor("数学");

        Activity activity = new Activity();
        activity.setRequiredMajors("计算机科学,软件工程");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenUserMajorIsNullButRequired() {
        User user = new User();

        Activity activity = new Activity();
        activity.setRequiredMajors("计算机科学");

        assertThat(activityService.isEligible(user, activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenSignupNotStarted() {
        Activity activity = new Activity();
        activity.setSignupStartTime(LocalDateTime.now().plusDays(1));

        assertThat(activityService.isEligible(new User(), activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenSignupEnded() {
        Activity activity = new Activity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(2));
        activity.setSignupEndTime(LocalDateTime.now().minusDays(1));

        assertThat(activityService.isEligible(new User(), activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnFalseWhenMaxParticipantsReached() {
        Activity activity = new Activity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(50);

        assertThat(activityService.isEligible(new User(), activity)).isFalse();
    }

    @Test
    void isEligibleShouldReturnTrueWhenMaxParticipantsIsNull() {
        Activity activity = new Activity();
        activity.setMaxParticipants(null);
        activity.setCurrentParticipants(100);

        assertThat(activityService.isEligible(new User(), activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenMaxParticipantsIsZero() {
        Activity activity = new Activity();
        activity.setMaxParticipants(0);
        activity.setCurrentParticipants(100);

        assertThat(activityService.isEligible(new User(), activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenCurrentParticipantsLessThanMax() {
        Activity activity = new Activity();
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        activity.setMaxParticipants(50);
        activity.setCurrentParticipants(49);

        assertThat(activityService.isEligible(new User(), activity)).isTrue();
    }

    @Test
    void isEligibleShouldReturnTrueWhenSignupTimeIsNull() {
        Activity activity = new Activity();

        assertThat(activityService.isEligible(new User(), activity)).isTrue();
    }

    @Test
    void isEligibleShouldMatchGradeInSingleGradeRequirement() {
        User user = new User();
        user.setGrade("大一");

        Activity activity = new Activity();
        activity.setRequiredGrades("大一");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }

    @Test
    void isEligibleShouldMatchMajorInSingleMajorRequirement() {
        User user = new User();
        user.setMajor("计算机科学");

        Activity activity = new Activity();
        activity.setRequiredMajors("计算机科学");

        assertThat(activityService.isEligible(user, activity)).isTrue();
    }
}
