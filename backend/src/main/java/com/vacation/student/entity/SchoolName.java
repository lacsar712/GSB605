package com.vacation.student.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchoolName {
    private Long id;
    /**
     * 标准学校名称
     */
    private String standardName;
    /**
     * 别名（逗号分隔）
     */
    private String aliases;
    /**
     * 使用次数（用于排序）
     */
    private Integer usageCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
