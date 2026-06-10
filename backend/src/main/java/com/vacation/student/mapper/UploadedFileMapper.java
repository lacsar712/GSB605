package com.vacation.student.mapper;

import com.vacation.student.entity.UploadedFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UploadedFileMapper {

    @Select("SELECT * FROM uploaded_files WHERE stored_name = #{storedName}")
    UploadedFile findByStoredName(String storedName);

    @Insert("INSERT INTO uploaded_files (stored_name, original_name, uploader_id, owner_type, owner_id, create_time, update_time) " +
            "VALUES (#{storedName}, #{originalName}, #{uploaderId}, #{ownerType}, #{ownerId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UploadedFile uploadedFile);

    @Update("UPDATE uploaded_files SET owner_type = #{ownerType}, owner_id = #{ownerId}, update_time = NOW() WHERE stored_name = #{storedName}")
    int updateOwner(@Param("storedName") String storedName, @Param("ownerType") String ownerType, @Param("ownerId") Long ownerId);
}
