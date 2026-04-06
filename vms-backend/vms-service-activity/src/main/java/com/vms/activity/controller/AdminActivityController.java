package com.vms.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.ActivityListVO;
import com.vms.activity.service.AdminActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员活动控制器
 * 提供管理员相关的活动审核接口
 */
@RestController
@RequestMapping("/api/activity/admin")
@RequiredArgsConstructor
public class AdminActivityController {

    private final AdminActivityService adminActivityService;
    private final UserContext userContext;

    /**
     * 管理员获取活动列表（支持状态筛选）
     */
    @GetMapping("/pending")
    public Result<Page<ActivityListVO>> listPending(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "orgName", required = false) String orgName,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader("Authorization") String authorization) {

        userContext.requireAdmin(authorization);

        Page<ActivityListVO> result = adminActivityService.listPending(page, size, keyword, orgName, status);
        return Result.success(result);
    }

    /**
     * 管理员审核活动
     */
    @PostMapping("/audit")
    public Result<Void> audit(
            @Valid @RequestBody ActivityAuditDTO dto,
            @RequestHeader("Authorization") String authorization) {

        userContext.requireAdmin(authorization);

        Long auditorId = userContext.getUserId(authorization);
        adminActivityService.audit(dto, auditorId);
        return Result.successMsg("审核完成");
    }
}