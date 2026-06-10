package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Registration {
    private Long id;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 招生对象学校
     */
    private String targetSchool;
    /**
     * 联系方式
     */
    private String contactInfo;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 附件（JSON格式）
     */
    private String attachments;
    /**
     * 审批状态: PENDING-待审批, APPROVED-通过, REJECTED-拒绝
     */
    private String approvalStatus;
    /**
     * 审批人ID
     */
    private Long approverId;
    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;
    /**
     * 审批意见
     */
    private String approvalComment;
    /**
     * 分组ID
     */
    private Long groupId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
