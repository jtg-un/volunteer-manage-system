package com.vms.service;

import com.vms.common.dto.LoginDTO;
import com.vms.common.dto.OrgRegisterDTO;
import com.vms.common.dto.VolunteerRegisterDTO;
import com.vms.common.vo.LoginVO;
import com.vms.common.vo.UserInfoVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 志愿者注册
     */
    void registerVolunteer(VolunteerRegisterDTO dto);

    /**
     * 组织注册（双表事务，状态为待审核）
     */
    void registerOrganization(OrgRegisterDTO dto);

    /**
     * 统一登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUser(Long userId);
}