package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 报名记录ID
     */
    private Long registrationId;
    /**
     * 提交用户ID
     */
    private Long userId;
    /**
     * 反馈标题
     */
    private String title;
    /**
     * 反馈内容（富文本）
     */
    private String content;
    /**
     * 附件（JSON格式）
     */
    private String attachments;
    /**
     * 反馈类型: SUMMARY-工作总结, PROGRESS-进度汇报, OTHER-其他
     */
    private String feedbackType;
    /**
     * 状态: 0-草稿, 1-已提交
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
