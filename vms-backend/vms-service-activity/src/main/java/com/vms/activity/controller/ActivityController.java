package com.vms.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.result.Result;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.common.vo.MyActivityListVO;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.service.ActivityService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final OrganizationMapper organizationMapper;

    @Value("${jwt.secret:vms-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    /**
     * 发布活动
     */
    @PostMapping("/publish")
    public Result<Long> publish(
            @Valid @RequestBody ActivityPublishDTO dto,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        Integer role = getRoleFromToken(authorization);

        // 验证是否为组织负责人
        if (role == null || role != 1) {
            throw new BusinessException(403, "只有组织可以发布活动");
        }

        // 通过 userId 查询 orgId
        LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(orgWrapper);

        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }

        if (org.getAuditStatus() != 1) {
            throw new BusinessException(403, "组织未通过审核，无法发布活动");
        }

        Long activityId = activityService.publish(org.getOrgId(), dto);
        return Result.success("活动发布成功，等待审核", activityId);
    }

    /**
     * 活动列表（分页）- 仅展示审核通过的活动
     */
    @GetMapping("/list")
    public Result<Page<ActivityListVO>> list(ActivityQueryDTO query) {
        // 默认只查询审核通过的活动（状态 0,1,2）
        if (query.getStatus() == null) {
            query.setStatusList(java.util.Arrays.asList(0, 1, 2));
        }
        Page<ActivityListVO> result = activityService.list(query);
        return Result.success(result);
    }

    /**
     * 活动详情
     */
    @GetMapping("/detail/{id}")
    public Result<ActivityDetailVO> detail(@PathVariable("id") Long id) {
        ActivityDetailVO vo = activityService.getDetail(id);
        return Result.success(vo);
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员获取活动列表（支持状态筛选）
     */
    @GetMapping("/admin/pending")
    public Result<Page<ActivityListVO>> listPending(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader("Authorization") String authorization) {
        Integer role = getRoleFromToken(authorization);
        if (role == null || role != 2) {
            throw new BusinessException(403, "无权限访问");
        }

        Page<ActivityListVO> result = activityService.listPending(page, size, status);
        return Result.success(result);
    }

    /**
     * 管理员审核活动
     */
    @PostMapping("/admin/audit")
    public Result<Void> audit(
            @Valid @RequestBody ActivityAuditDTO dto,
            @RequestHeader("Authorization") String authorization) {
        Integer role = getRoleFromToken(authorization);
        if (role == null || role != 2) {
            throw new BusinessException(403, "无权限访问");
        }

        Long auditorId = getUserIdFromToken(authorization);
        activityService.audit(dto, auditorId);
        return Result.successMsg("审核完成");
    }

    // ==================== 组织接口 ====================

    /**
     * 组织获取自己的活动列表
     */
    @GetMapping("/my/list")
    public Result<Page<MyActivityListVO>> myList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        Integer role = getRoleFromToken(authorization);

        if (role == null || role != 1) {
            throw new BusinessException(403, "只有组织可以访问");
        }

        // 通过 userId 查询 orgId
        LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(orgWrapper);

        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }

        Page<MyActivityListVO> result = activityService.listByOrg(org.getOrgId(), page, size);
        return Result.success(result);
    }

    /**
     * 组织更新活动状态（待启动->运行中，运行中->已结项）
     */
    @PutMapping("/my/status")
    public Result<Void> updateStatus(
            @Valid @RequestBody ActivityStatusDTO dto,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        Integer role = getRoleFromToken(authorization);

        if (role == null || role != 1) {
            throw new BusinessException(403, "只有组织可以操作");
        }

        // 通过 userId 查询 orgId
        LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(orgWrapper);

        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }

        activityService.updateStatus(dto, org.getOrgId());
        return Result.successMsg("状态更新成功");
    }

    // ==================== 辅助方法 ====================

    private Long getUserIdFromToken(String authorization) {
        String token = authorization.replace("Bearer ", "");
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }

    private Integer getRoleFromToken(String authorization) {
        String token = authorization.replace("Bearer ", "");
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", Integer.class);
    }
}