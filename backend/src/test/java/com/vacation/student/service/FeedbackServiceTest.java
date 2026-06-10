package com.vacation.student.service;

import com.vacation.student.dto.FeedbackDTO;
import com.vacation.student.entity.Activity;
import com.vacation.student.entity.Feedback;
import com.vacation.student.entity.Registration;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.FeedbackMapper;
import com.vacation.student.mapper.RegistrationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void createShouldRejectBeforeFeedbackWindowStarts() {
        Registration registration = approvedRegistration();
        Activity activity = new Activity();
        activity.setId(5L);
        activity.setFeedbackStartTime(LocalDateTime.now().plusDays(1));
        activity.setFeedbackEndTime(LocalDateTime.now().plusDays(10));

        when(registrationMapper.findById(9L)).thenReturn(registration);
        when(activityMapper.findById(5L)).thenReturn(activity);

        assertThatThrownBy(() -> feedbackService.create(3L, validDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未开始");
    }

    @Test
    void createShouldSanitizeRichTextBeforeSaving() {
        Registration registration = approvedRegistration();
        Activity activity = new Activity();
        activity.setId(5L);
        activity.setFeedbackStartTime(LocalDateTime.now().minusDays(1));
        activity.setFeedbackEndTime(LocalDateTime.now().plusDays(10));

        FeedbackDTO dto = validDto();
        dto.setContent("<p>正常内容</p><script>alert('xss')</script><img src=x onerror=alert(1)>");

        when(registrationMapper.findById(9L)).thenReturn(registration);
        when(activityMapper.findById(5L)).thenReturn(activity);

        feedbackService.create(3L, dto);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());
        assertThat(captor.getValue().getContent()).contains("<p>正常内容</p>");
        assertThat(captor.getValue().getContent()).doesNotContain("<script");
        assertThat(captor.getValue().getContent()).doesNotContain("onerror");
    }

    private Registration approvedRegistration() {
        Registration registration = new Registration();
        registration.setId(9L);
        registration.setActivityId(5L);
        registration.setUserId(3L);
        registration.setApprovalStatus("APPROVED");
        return registration;
    }

    private FeedbackDTO validDto() {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setActivityId(5L);
        dto.setRegistrationId(9L);
        dto.setTitle("进度反馈");
        dto.setContent("<p>内容</p>");
        dto.setFeedbackType("PROGRESS");
        return dto;
    }
}
