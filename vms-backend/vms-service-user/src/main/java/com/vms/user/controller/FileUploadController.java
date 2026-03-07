package com.vms.user.controller;

import com.vms.common.context.UserContext;
import com.vms.common.result.Result;
import com.vms.user.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserContext userContext;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.getUserId(authorization);
        String avatarUrl = fileUploadService.uploadAvatar(userId, file);

        Map<String, String> result = new HashMap<>();
        result.put("url", avatarUrl);
        return Result.success(result);
    }
}