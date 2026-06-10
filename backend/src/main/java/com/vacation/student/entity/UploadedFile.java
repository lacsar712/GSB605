package com.vacation.student.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UploadedFile {
    private Long id;
    private String storedName;
    private String originalName;
    private Long uploaderId;
    private String ownerType;
    private Long ownerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
