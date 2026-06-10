package com.vacation.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    @NotNull(message = "报名记录ID不能为空")
    private Long registrationId;
    
    @NotBlank(message = "反馈标题不能为空")
    private String title;
    
    @NotBlank(message = "反馈内容不能为空")
    private String content;
    
    private String attachments;
    
    private String feedbackType;
}
