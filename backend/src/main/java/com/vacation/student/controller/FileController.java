package com.vacation.student.controller;

import com.vacation.student.dto.Result;
import com.vacation.student.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute Long userId) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        // 检查文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.error("文件大小不能超过50MB");
        }
        
        try {
            var uploadedFile = fileService.uploadFile(file, userId);
            Map<String, String> data = new HashMap<>();
            data.put("url", "/api/files/download/" + uploadedFile.getStoredName());
            data.put("name", uploadedFile.getOriginalName());
            data.put("storedName", uploadedFile.getStoredName());
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(
            @PathVariable String filename,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        try {
            byte[] data = fileService.downloadFile(filename, userId, role);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{filename}")
    public Result<Void> delete(
            @PathVariable String filename,
            @RequestAttribute Long userId,
            @RequestAttribute String role) {
        try {
            fileService.deleteFile(filename, userId, role);
            return Result.success("删除成功", null);
        } catch (IOException e) {
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
