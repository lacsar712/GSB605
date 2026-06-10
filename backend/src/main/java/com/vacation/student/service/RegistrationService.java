package com.vacation.student.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacation.student.dto.RegistrationDTO;
import com.vacation.student.entity.Activity;
import com.vacation.student.entity.ActivityGroup;
import com.vacation.student.entity.Registration;
import com.vacation.student.entity.User;
import com.vacation.student.mapper.ActivityGroupMapper;
import com.vacation.student.mapper.ActivityMapper;
import com.vacation.student.mapper.RegistrationMapper;
import com.vacation.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistrationService {
    
    @Autowired
    private RegistrationMapper registrationMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ActivityGroupMapper activityGroupMapper;
    
    @Autowired
    private SchoolNameService schoolNameService;
    
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileService fileService;
    
    public Registration findById(Long id) {
        return registrationMapper.findById(id);
    }
    
    public Registration findByActivityAndUser(Long activityId, Long userId) {
        return registrationMapper.findByActivityAndUser(activityId, userId);
    }
    
    public List<Registration> findByUserId(Long userId) {
        return registrationMapper.findByUserId(userId);
    }
    
    public List<Registration> findByActivityId(Long activityId) {
        return registrationMapper.findByActivityId(activityId);
    }
    
    public List<Registration> findByActivityIdAndStatus(Long activityId, String status) {
        return registrationMapper.findByActivityIdAndStatus(activityId, status);
    }
    
    public List<Registration> findByGroupId(Long groupId) {
        return registrationMapper.findByGroupId(groupId);
    }
    
    @Transactional
    public Registration register(Long userId, RegistrationDTO dto) {
        Activity activity = activityMapper.findById(dto.getActivityId());
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 检查是否已报名
        Registration existing = registrationMapper.findByActivityAndUser(dto.getActivityId(), userId);
        if (existing != null) {
            throw new IllegalArgumentException("您已报名该活动，请勿重复报名");
        }
        
        // 检查是否有资格
        if (!activityService.isEligible(user, activity)) {
            throw new IllegalArgumentException("您不符合该活动的报名条件");
        }

        validateConfiguredFields(activity, dto);
        
        // 标准化学校名称
        String standardSchool = dto.getTargetSchool();
        if (standardSchool != null && !standardSchool.isEmpty()) {
            standardSchool = schoolNameService.standardize(standardSchool);
        }
        
        // 创建或获取分组
        Long groupId = null;
        if (standardSchool != null && !standardSchool.isEmpty()) {
            ActivityGroup group = activityGroupMapper.findByActivityIdAndName(dto.getActivityId(), standardSchool);
            if (group == null) {
                group = new ActivityGroup();
                group.setActivityId(dto.getActivityId());
                group.setGroupName(standardSchool);
                group.setDescription("按学校分组: " + standardSchool);
                activityGroupMapper.insert(group);
            }
            groupId = group.getId();
            activityGroupMapper.incrementMemberCount(groupId);
        }
        
        // 创建报名记录
        Registration registration = new Registration();
        registration.setActivityId(dto.getActivityId());
        registration.setUserId(userId);
        registration.setTargetSchool(standardSchool);
        registration.setContactInfo(dto.getContactInfo());
        registration.setRemark(dto.getRemark());
        registration.setAttachments(dto.getAttachments());
        registration.setGroupId(groupId);
        
        registrationMapper.insert(registration);
        fileService.bindFiles(registration.getAttachments(), "REGISTRATION", registration.getId());
        
        // 更新活动报名人数
        activityMapper.incrementParticipants(dto.getActivityId());
        
        // 如果不需要审批，直接通过
        if (activity.getNeedApproval() == null || activity.getNeedApproval() == 0) {
            registration.setApprovalStatus("APPROVED");
            registrationMapper.updateApproval(registration);
        }
        
        return registration;
    }
    
    @Transactional
    public Registration approve(Long id, Long approverId, String status, String comment) {
        Registration registration = registrationMapper.findById(id);
        if (registration == null) {
            throw new IllegalArgumentException("报名记录不存在");
        }
        
        registration.setApprovalStatus(status);
        registration.setApproverId(approverId);
        registration.setApprovalComment(comment);
        
        registrationMapper.updateApproval(registration);
        
        return registration;
    }
    
    public void updateAttachments(Long id, String attachments) {
        registrationMapper.updateAttachments(id, attachments);
    }
    
    public void delete(Long id) {
        Registration registration = registrationMapper.findById(id);
        if (registration != null && registration.getGroupId() != null) {
            activityGroupMapper.decrementMemberCount(registration.getGroupId());
        }
        registrationMapper.deleteById(id);
    }

    private void validateConfiguredFields(Activity activity, RegistrationDTO dto) {
        if (activity.getRegistrationConfig() == null || activity.getRegistrationConfig().isBlank()) {
            return;
        }

        try {
            List<Map<String, Object>> fields = objectMapper.readValue(
                    activity.getRegistrationConfig(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            List<String> missingFields = new ArrayList<>();
            for (Map<String, Object> field : fields) {
                Object requiredValue = field.get("required");
                if (!(requiredValue instanceof Boolean required) || !required) {
                    continue;
                }

                String key = String.valueOf(field.getOrDefault("key", ""));
                String label = String.valueOf(field.getOrDefault("label", key));
                if (isBlank(resolveFieldValue(key, dto))) {
                    missingFields.add(label);
                }
            }

            if (!missingFields.isEmpty()) {
                throw new IllegalArgumentException("请完善报名信息: " + String.join("、", missingFields));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("活动报名配置无效");
        }
    }

    private String resolveFieldValue(String key, RegistrationDTO dto) {
        Map<String, String> values = new HashMap<>();
        values.put("targetSchool", dto.getTargetSchool());
        values.put("contactInfo", dto.getContactInfo());
        values.put("remark", dto.getRemark());
        values.put("attachments", dto.getAttachments());
        return values.get(key);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
