package com.vms.common.context;

import com.vms.common.exception.BusinessException;
import com.vms.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户上下文辅助类
 * 封装 Token 解析和权限验证逻辑
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final JwtUtils jwtUtils;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 从 Authorization Header 获取用户ID
     */
    public Long getUserId(String authorization) {
        String token = extractToken(authorization);
        return jwtUtils.getUserId(token);
    }

    /**
     * 从 Authorization Header 获取角色
     */
    public Integer getRole(String authorization) {
        String token = extractToken(authorization);
        return jwtUtils.getRole(token);
    }

    /**
     * 验证是否为管理员角色
     * @throws BusinessException 如果不是管理员
     */
    public void requireAdmin(String authorization) {
        Integer role = getRole(authorization);
        if (role == null || role != 2) {
            throw new BusinessException(403, "无权限访问");
        }
    }

    /**
     * 验证是否为组织角色
     * @throws BusinessException 如果不是组织
     */
    public void requireOrg(String authorization) {
        Integer role = getRole(authorization);
        if (role == null || role != 1) {
            throw new BusinessException(403, "只有组织可以操作");
        }
    }

    /**
     * 验证是否为组织或管理员角色
     * @throws BusinessException 如果不是组织或管理员
     */
    public void requireOrgOrAdmin(String authorization) {
        Integer role = getRole(authorization);
        if (role == null || (role != 1 && role != 2)) {
            throw new BusinessException(403, "只有组织或管理员可以操作");
        }
    }

    /**
     * 验证是否为组织角色并返回用户ID
     */
    public Long requireOrgAndGetUserId(String authorization) {
        requireOrg(authorization);
        return getUserId(authorization);
    }

    /**
     * 验证是否为组织或管理员角色并返回用户ID
     */
    public Long requireOrgOrAdminAndGetUserId(String authorization) {
        requireOrgOrAdmin(authorization);
        return getUserId(authorization);
    }

    /**
     * 从 Authorization Header 提取 Token
     */
    private String extractToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            throw new BusinessException(401, "未提供认证信息");
        }
        if (authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return authorization;
    }
}