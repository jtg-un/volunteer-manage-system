package com.vms.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.OrgAuditDTO;
import com.vms.common.dto.OrgUpdateDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.result.Result;
import com.vms.common.vo.OrgDetailVO;
import com.vms.common.vo.OrgListVO;
import com.vms.user.service.OrganizationService;
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
 * 组织控制器
 */
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Value("${jwt.secret:vms-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    // ==================== 管理员接口 ====================

    /**
     * 获取组织列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Page<OrgListVO>> getOrgList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "auditStatus", required = false) Integer auditStatus,
            @RequestHeader("Authorization") String authorization) {
        // 验证管理员权限
        if (!isAdmin(authorization)) {
            throw new BusinessException(403, "无权限访问");
        }

        Page<OrgListVO> result = organizationService.getPendingList(page, size, keyword, auditStatus);
        return Result.success(result);
    }

    /**
     * 获取组织详情（管理员）
     */
    @GetMapping("/admin/detail/{orgId}")
    public Result<OrgDetailVO> getOrgDetail(
            @PathVariable("orgId") Long orgId,
            @RequestHeader("Authorization") String authorization) {
        // 验证管理员权限
        if (!isAdmin(authorization)) {
            throw new BusinessException(403, "无权限访问");
        }

        OrgDetailVO vo = organizationService.getOrgDetail(orgId);
        return Result.success(vo);
    }

    /**
     * 审核组织（管理员）
     */
    @PostMapping("/admin/audit")
    public Result<Void> auditOrg(
            @Valid @RequestBody OrgAuditDTO dto,
            @RequestHeader("Authorization") String authorization) {
        // 验证管理员权限
        Long userId = getUserIdFromToken(authorization);
        if (!isAdmin(authorization)) {
            throw new BusinessException(403, "无权限访问");
        }

        organizationService.auditOrg(userId, dto);
        return Result.successMsg("审核成功");
    }

    // ==================== 组织接口 ====================

    /**
     * 获取当前组织信息
     */
    @GetMapping("/my")
    public Result<OrgDetailVO> getMyOrg(@RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        OrgDetailVO vo = organizationService.getMyOrg(userId);
        return Result.success(vo);
    }

    /**
     * 更新组织信息
     */
    @PutMapping("/my")
    public Result<Void> updateOrg(
            @Valid @RequestBody OrgUpdateDTO dto,
            @RequestHeader("Authorization") String authorization) {
        Long userId = getUserIdFromToken(authorization);
        organizationService.updateOrg(userId, dto);
        return Result.successMsg("更新成功");
    }

    // ==================== 公开接口 ====================

    /**
     * 获取组织详情（公开，所有登录用户可访问）
     * 用于志愿者查看组织信息
     */
    @GetMapping("/public/{orgId}")
    public Result<OrgDetailVO> getPublicOrgDetail(@PathVariable("orgId") Long orgId) {
        OrgDetailVO vo = organizationService.getOrgDetail(orgId);
        return Result.success(vo);
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

    private boolean isAdmin(String authorization) {
        Integer role = getRoleFromToken(authorization);
        return role != null && role == 2;
    }
}