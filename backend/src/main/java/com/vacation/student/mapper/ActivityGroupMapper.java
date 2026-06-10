package com.vacation.student.mapper;

import com.vacation.student.entity.ActivityGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityGroupMapper {
    
    @Select("SELECT * FROM activity_groups WHERE id = #{id}")
    ActivityGroup findById(Long id);
    
    @Select("SELECT * FROM activity_groups WHERE activity_id = #{activityId} ORDER BY group_name")
    List<ActivityGroup> findByActivityId(Long activityId);
    
    @Select("SELECT * FROM activity_groups WHERE activity_id = #{activityId} AND group_name = #{groupName}")
    ActivityGroup findByActivityIdAndName(@Param("activityId") Long activityId, @Param("groupName") String groupName);
    
    @Insert("INSERT INTO activity_groups (activity_id, group_name, description, member_count, create_time, update_time) " +
            "VALUES (#{activityId}, #{groupName}, #{description}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivityGroup group);
    
    @Update("UPDATE activity_groups SET group_name = #{groupName}, description = #{description}, update_time = NOW() WHERE id = #{id}")
    int update(ActivityGroup group);
    
    @Update("UPDATE activity_groups SET member_count = member_count + 1, update_time = NOW() WHERE id = #{id}")
    int incrementMemberCount(Long id);
    
    @Update("UPDATE activity_groups SET member_count = member_count - 1, update_time = NOW() WHERE id = #{id} AND member_count > 0")
    int decrementMemberCount(Long id);
    
    @Delete("DELETE FROM activity_groups WHERE id = #{id}")
    int deleteById(Long id);
}
