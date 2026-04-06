package com.vms.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.UserStatusDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.UserInfoVO;
import com.vms.user.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 * 提供用户账号管理接口
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final UserContext userContext;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<Page<UserInfoVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorization) {

        userContext.requireAdmin(authorization);
        Page<UserInfoVO> result = userAdminService.listUsers(keyword, role, status, page, size);
        return Result.success(result);
    }

    /**
     * 更新用户状态（封禁/启用）
     */
    @PutMapping("/status")
    public Result<Void> updateStatus(
            @Valid @RequestBody UserStatusDTO dto,
            @RequestHeader("Authorization") String authorization) {

        userContext.requireAdmin(authorization);
        userAdminService.updateStatus(dto);
        return Result.successMsg("状态更新成功");
    }
}