package com.vms.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.result.Result;
import com.vms.common.vo.MyActivityListVO;
import com.vms.activity.service.OrgActivityService;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.OrganizationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 组织活动控制器
 * 提供组织相关的活动管理接口
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class OrgActivityController {

    private final OrgActivityService orgActivityService;
    private final OrganizationMapper organizationMapper;
    private final UserContext userContext;

    /**
     * 发布活动
     */
    @PostMapping("/publish")
    public Result<Long> publish(
            @Valid @RequestBody ActivityPublishDTO dto,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Organization org = getOrgByUserId(userId);

        if (org.getAuditStatus() != 1) {
            throw new BusinessException(403, "组织未通过审核，无法发布活动");
        }

        Long activityId = orgActivityService.publish(org.getOrgId(), dto);
        return Result.success("活动发布成功，等待审核", activityId);
    }

    /**
     * 组织获取自己的活动列表
     */
    @GetMapping("/my/list")
    public Result<Page<MyActivityListVO>> myList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Organization org = getOrgByUserId(userId);

        Page<MyActivityListVO> result = orgActivityService.listByOrg(org.getOrgId(), page, size);
        return Result.success(result);
    }

    /**
     * 组织更新活动状态
     */
    @PutMapping("/my/status")
    public Result<Void> updateStatus(
            @Valid @RequestBody ActivityStatusDTO dto,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Organization org = getOrgByUserId(userId);

        orgActivityService.updateStatus(dto, org.getOrgId());
        return Result.successMsg("状态更新成功");
    }

    /**
     * 根据用户ID获取组织信息
     */
    private Organization getOrgByUserId(Long userId) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(wrapper);
        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }
        return org;
    }
}