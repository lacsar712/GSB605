package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    /**
     * 活动标题
     */
    private String title;
    /**
     * 活动简介
     */
    private String summary;
    /**
     * 活动详情（富文本）
     */
    private String content;
    /**
     * 封面图片
     */
    private String coverImage;
    /**
     * 活动类型: ONLINE-线上, OFFLINE-线下
     */
    private String activityType;
    /**
     * 报名开始时间
     */
    private LocalDateTime signupStartTime;
    /**
     * 报名截止时间
     */
    private LocalDateTime signupEndTime;
    /**
     * 活动开始时间
     */
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间
     */
    private LocalDateTime activityEndTime;
    /**
     * 活动地点
     */
    private String location;
    /**
     * 招生人数限制
     */
    private Integer maxParticipants;
    /**
     * 当前报名人数
     */
    private Integer currentParticipants;
    /**
     * 绩点要求
     */
    private Double requiredGpa;
    /**
     * 年级要求（逗号分隔）
     */
    private String requiredGrades;
    /**
     * 专业要求（逗号分隔）
     */
    private String requiredMajors;
    /**
     * 是否需要审批: 0-否, 1-是
     */
    private Integer needApproval;
    /**
     * 是否轮播展示: 0-否, 1-是
     */
    private Integer isBanner;
    /**
     * 轮播图片
     */
    private String bannerImage;
    /**
     * 附件列表（JSON格式）
     */
    private String attachments;
    /**
     * 报名字段配置（JSON格式）
     */
    private String registrationConfig;
    /**
     * 反馈开始时间
     */
    private LocalDateTime feedbackStartTime;
    /**
     * 反馈截止时间
     */
    private LocalDateTime feedbackEndTime;
    /**
     * 状态: 0-草稿, 1-发布, 2-结束
     */
    private Integer status;
    /**
     * 创建人ID
     */
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
