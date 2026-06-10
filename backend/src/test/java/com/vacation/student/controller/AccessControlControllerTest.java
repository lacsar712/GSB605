package com.vacation.student.controller;

import com.vacation.student.dto.Result;
import com.vacation.student.entity.Feedback;
import com.vacation.student.entity.Registration;
import com.vacation.student.entity.User;
import com.vacation.student.service.FeedbackService;
import com.vacation.student.service.RegistrationService;
import com.vacation.student.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private RegistrationController registrationController;

    @InjectMocks
    private FeedbackController feedbackController;

    @Test
    void userControllerShouldRejectStudentReadingAnotherUserById() {
        Result<User> result = userController.getById(20L, 3L, "STUDENT");

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void registrationControllerShouldRejectStudentReadingAnotherUsersRegistration() {
        Registration registration = new Registration();
        registration.setId(7L);
        registration.setUserId(99L);
        when(registrationService.findById(7L)).thenReturn(registration);

        Result<Registration> result = registrationController.getById(7L, 3L, "STUDENT");

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void feedbackControllerShouldRejectStudentReadingAnotherUsersFeedback() {
        Feedback feedback = new Feedback();
        feedback.setId(6L);
        feedback.setUserId(88L);
        when(feedbackService.findById(6L)).thenReturn(feedback);

        Result<Feedback> result = feedbackController.getById(6L, 3L, "STUDENT");

        assertThat(result.getCode()).isEqualTo(403);
    }
}
