package com.vms.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.HoursConfirmDTO;
import com.vms.common.dto.RegistrationAuditDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.RegistrationListVO;
import com.vms.activity.service.OrgRegistrationService;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.OrganizationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 组织端报名管理控制器
 */
@RestController
@RequestMapping("/api/org/registration")
@RequiredArgsConstructor
public class OrgRegistrationController {

    private final OrgRegistrationService registrationService;
    private final OrganizationMapper organizationMapper;
    private final UserContext userContext;

    /**
     * 获取活动的报名列表
     */
    @GetMapping("/list/{activityId}")
    public Result<Page<RegistrationListVO>> listRegistrations(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("activityId") Long activityId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Long orgId = getOrgIdByUserId(userId);
        Page<RegistrationListVO> result = registrationService.listRegistrations(orgId, activityId, status, page, size);
        return Result.success(result);
    }

    /**
     * 审核报名
     */
    @PostMapping("/audit")
    public Result<Void> audit(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RegistrationAuditDTO dto) {
        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Long orgId = getOrgIdByUserId(userId);
        registrationService.audit(orgId, dto);
        return Result.successMsg("审核完成");
    }

    /**
     * 确认发放时长
     */
    @PostMapping("/hours")
    public Result<Void> confirmHours(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody HoursConfirmDTO dto) {
        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Long orgId = getOrgIdByUserId(userId);
        registrationService.confirmHours(orgId, dto);
        return Result.successMsg("时长发放成功");
    }

    /**
     * 根据用户ID获取组织ID
     */
    private Long getOrgIdByUserId(Long userId) {
        Organization org = organizationMapper.selectByUserId(userId);
        return org != null ? org.getOrgId() : null;
    }
}