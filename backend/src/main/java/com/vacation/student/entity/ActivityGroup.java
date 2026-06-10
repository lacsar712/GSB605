package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityGroup {
    private Long id;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 分组名称（如学校名称）
     */
    private String groupName;
    /**
     * 分组描述
     */
    private String description;
    /**
     * 成员数量
     */
    private Integer memberCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
