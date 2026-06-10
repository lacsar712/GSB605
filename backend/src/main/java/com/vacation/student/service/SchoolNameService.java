package com.vacation.student.service;

import com.vacation.student.entity.SchoolName;
import com.vacation.student.mapper.SchoolNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SchoolNameService {
    
    @Autowired
    private SchoolNameMapper schoolNameMapper;
    
    // 常见大学简称映射
    private static final String[][] SCHOOL_ALIASES = {
            {"清华大学", "清华"},
            {"北京大学", "北大"},
            {"武汉大学", "武大"},
            {"武汉理工大学", "武汉理工", "理工大"},
            {"华中科技大学", "华科", "华中科技"},
            {"中国人民大学", "人大", "人民大学"},
            {"复旦大学", "复旦"},
            {"上海交通大学", "上交", "交大"},
            {"浙江大学", "浙大"},
            {"南京大学", "南大"},
            {"中山大学", "中大"},
            {"四川大学", "川大"},
            {"山东大学", "山大"},
            {"厦门大学", "厦大"},
            {"同济大学", "同济"},
            {"北京航空航天大学", "北航"},
            {"北京理工大学", "北理工"},
            {"西安交通大学", "西交"},
            {"哈尔滨工业大学", "哈工大"},
            {"华南理工大学", "华南理工"},
    };
    
    public List<SchoolName> searchByKeyword(String keyword) {
        return schoolNameMapper.searchByKeyword(keyword);
    }
    
    public List<SchoolName> findAll() {
        return schoolNameMapper.findAll();
    }
    
    /**
     * 标准化学校名称
     * 使用简单的规则匹配，将简称转换为标准全称
     */
    public String standardize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String trimmed = input.trim();
        
        // 首先在数据库中查找
        SchoolName existing = schoolNameMapper.findByStandardName(trimmed);
        if (existing != null) {
            schoolNameMapper.incrementUsageCount(existing.getId());
            return existing.getStandardName();
        }
        
        // 检查是否匹配已知简称
        for (String[] aliasGroup : SCHOOL_ALIASES) {
            String standardName = aliasGroup[0];
            for (int i = 1; i < aliasGroup.length; i++) {
                if (trimmed.equals(aliasGroup[i]) || trimmed.contains(aliasGroup[i])) {
                    // 查找或创建标准名称记录
                    SchoolName standard = schoolNameMapper.findByStandardName(standardName);
                    if (standard == null) {
                        SchoolName newSchool = new SchoolName();
                        newSchool.setStandardName(standardName);
                        newSchool.setAliases(String.join(",", Arrays.copyOfRange(aliasGroup, 1, aliasGroup.length)));
                        schoolNameMapper.insert(newSchool);
                    } else {
                        schoolNameMapper.incrementUsageCount(standard.getId());
                    }
                    return standardName;
                }
            }
            
            // 完全匹配标准名称
            if (trimmed.equals(standardName)) {
                SchoolName standard = schoolNameMapper.findByStandardName(standardName);
                if (standard == null) {
                    SchoolName newSchool = new SchoolName();
                    newSchool.setStandardName(standardName);
                    newSchool.setAliases(String.join(",", Arrays.copyOfRange(aliasGroup, 1, aliasGroup.length)));
                    schoolNameMapper.insert(newSchool);
                } else {
                    schoolNameMapper.incrementUsageCount(standard.getId());
                }
                return standardName;
            }
        }
        
        // 在数据库别名中搜索
        List<SchoolName> matches = schoolNameMapper.searchByKeyword(trimmed);
        if (!matches.isEmpty()) {
            SchoolName best = matches.get(0);
            schoolNameMapper.incrementUsageCount(best.getId());
            return best.getStandardName();
        }
        
        // 如果是新的学校名称，添加到数据库
        SchoolName newSchool = new SchoolName();
        newSchool.setStandardName(trimmed);
        newSchool.setAliases("");
        schoolNameMapper.insert(newSchool);
        
        return trimmed;
    }
    
    /**
     * 获取学校名称建议（用于自动完成）
     */
    public List<String> suggest(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        List<SchoolName> matches = schoolNameMapper.searchByKeyword(keyword.trim());
        return matches.stream()
                .map(SchoolName::getStandardName)
                .toList();
    }
}
