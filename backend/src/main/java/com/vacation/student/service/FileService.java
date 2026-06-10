package com.vacation.student.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacation.student.entity.Activity;
import com.vacation.student.entity.Feedback;
import com.vacation.student.entity.Registration;
import com.vacation.student.entity.UploadedFile;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.FeedbackMapper;
import com.vacation.student.mapper.RegistrationMapper;
import com.vacation.student.mapper.UploadedFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UploadedFile uploadFile(MultipartFile file, Long userId) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setStoredName(newFilename);
        uploadedFile.setOriginalName(originalFilename != null ? originalFilename : newFilename);
        uploadedFile.setUploaderId(userId);
        uploadedFile.setOwnerType("TEMP");
        uploadedFileMapper.insert(uploadedFile);
        return uploadedFile;
    }

    public void deleteFile(String storedName, Long userId, String role) throws IOException {
        if (!canAccess(storedName, userId, role)) {
            throw new IllegalArgumentException("无权删除该文件");
        }

        UploadedFile uploadedFile = requireUploadedFile(storedName);
        if ("TEMP".equals(uploadedFile.getOwnerType()) && !uploadedFile.getUploaderId().equals(userId) && !"SCHOOL".equals(role)) {
            throw new IllegalArgumentException("无权删除该文件");
        }

        Path filePath = resolveStoredPath(storedName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    public byte[] downloadFile(String storedName, Long userId, String role) throws IOException {
        if (!canAccess(storedName, userId, role)) {
            throw new IllegalArgumentException("无权访问该文件");
        }

        Path filePath = resolveStoredPath(storedName);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("文件不存在");
        }

        return Files.readAllBytes(filePath);
    }

    public void bindFiles(String attachments, String ownerType, Long ownerId) {
        for (String storedName : extractStoredNames(attachments)) {
            uploadedFileMapper.updateOwner(storedName, ownerType, ownerId);
        }
    }

    private boolean canAccess(String storedName, Long userId, String role) {
        UploadedFile uploadedFile = requireUploadedFile(storedName);
        if ("SCHOOL".equals(role)) {
            return true;
        }
        return switch (uploadedFile.getOwnerType()) {
            case "TEMP" -> uploadedFile.getUploaderId().equals(userId);
            case "ACTIVITY" -> canAccessActivity(uploadedFile.getOwnerId(), userId, role);
            case "REGISTRATION" -> canAccessRegistration(uploadedFile.getOwnerId(), userId, role);
            case "FEEDBACK" -> canAccessFeedback(uploadedFile.getOwnerId(), userId, role);
            case "USER" -> uploadedFile.getOwnerId() != null && uploadedFile.getOwnerId().equals(userId);
            default -> false;
        };
    }

    private boolean canAccessActivity(Long activityId, Long userId, String role) {
        Activity activity = activityMapper.findById(activityId);
        if (activity == null) {
            return false;
        }
        if ("TEACHER".equals(role)) {
            return activity.getCreatorId() != null && activity.getCreatorId().equals(userId);
        }
        return "STUDENT".equals(role) && activity.getStatus() != null && activity.getStatus() == 1;
    }

    private boolean canAccessRegistration(Long registrationId, Long userId, String role) {
        Registration registration = registrationMapper.findById(registrationId);
        if (registration == null) {
            return false;
        }
        if ("STUDENT".equals(role)) {
            return registration.getUserId().equals(userId);
        }
        if ("TEACHER".equals(role)) {
            Activity activity = activityMapper.findById(registration.getActivityId());
            return activity != null && activity.getCreatorId() != null && activity.getCreatorId().equals(userId);
        }
        return false;
    }

    private boolean canAccessFeedback(Long feedbackId, Long userId, String role) {
        Feedback feedback = feedbackMapper.findById(feedbackId);
        if (feedback == null) {
            return false;
        }
        if ("STUDENT".equals(role)) {
            return feedback.getUserId().equals(userId);
        }
        if ("TEACHER".equals(role)) {
            Activity activity = activityMapper.findById(feedback.getActivityId());
            return activity != null && activity.getCreatorId() != null && activity.getCreatorId().equals(userId);
        }
        return false;
    }

    private UploadedFile requireUploadedFile(String storedName) {
        UploadedFile uploadedFile = uploadedFileMapper.findByStoredName(storedName);
        if (uploadedFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        return uploadedFile;
    }

    private Path resolveStoredPath(String storedName) {
        return Paths.get(uploadDir).resolve(storedName).normalize();
    }

    private List<String> extractStoredNames(String attachments) {
        if (attachments == null || attachments.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> files = objectMapper.readValue(
                    attachments,
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            return files.stream()
                    .map(this::resolveStoredName)
                    .filter(name -> name != null && !name.isBlank())
                    .distinct()
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String resolveStoredName(Map<String, Object> file) {
        Object storedName = file.get("storedName");
        if (storedName instanceof String stored && !stored.isBlank()) {
            return stored;
        }
        Object url = file.get("url");
        if (url instanceof String urlString && !urlString.isBlank()) {
            int index = urlString.lastIndexOf('/');
            if (index >= 0 && index + 1 < urlString.length()) {
                return urlString.substring(index + 1);
            }
            return urlString;
        }
        return null;
    }
}
