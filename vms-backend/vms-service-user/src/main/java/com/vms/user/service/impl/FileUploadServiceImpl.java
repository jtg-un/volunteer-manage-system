package com.vms.user.service.impl;

import com.vms.common.exception.BusinessException;
import com.vms.user.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url-prefix}")
    private String urlPrefix;

    // 允许的图片类型
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 最大文件大小：5MB
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 验证文件
        validateFile(file);

        // 生成文件名：用户ID_时间戳.扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFilename = userId + "_" + System.currentTimeMillis() + extension;

        // 创建日期目录
        String dirPath = uploadPath + datePath + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        String filePath = dirPath + newFilename;
        try {
            file.transferTo(new File(filePath));
            log.info("头像上传成功: {}", filePath);
        } catch (IOException e) {
            log.error("头像保存失败", e);
            throw new BusinessException(500, "文件保存失败");
        }

        // 返回访问URL
        return urlPrefix + datePath + "/" + newFilename;
    }

    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }

        // 验证文件大小
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(400, "只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}