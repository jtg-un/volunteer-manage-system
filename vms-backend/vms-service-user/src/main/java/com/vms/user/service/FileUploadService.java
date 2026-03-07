package com.vms.user.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像访问URL
     */
    String uploadAvatar(Long userId, MultipartFile file);
}