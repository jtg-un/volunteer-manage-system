package com.vms.user.service;

import com.vms.common.dto.UserStatusDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.vo.UserInfoVO;

/**
 * 管理员用户服务接口
 * 提供管理员对用户账号的管理功能
 */
public interface UserAdminService {

    /**
     * 获取用户列表（分页）
     * @param role 角色筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    Page<UserInfoVO> listUsers(Integer role, int page, int size);

    /**
     * 更新用户状态（封禁/启用）
     * @param dto 状态信息
     */
    void updateStatus(UserStatusDTO dto);
}