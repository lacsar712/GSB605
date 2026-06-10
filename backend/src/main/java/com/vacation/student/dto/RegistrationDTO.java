package com.vacation.student.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    private String targetSchool;
    
    private String contactInfo;
    
    private String remark;
    
    private String attachments;
}
