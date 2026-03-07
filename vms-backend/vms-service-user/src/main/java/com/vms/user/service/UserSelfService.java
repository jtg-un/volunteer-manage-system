package com.vms.user.service;

import com.vms.common.dto.PasswordUpdateDTO;
import com.vms.common.dto.UserUpdateDTO;
import com.vms.common.vo.UserInfoVO;

/**
 * 用户个人服务接口
 * 提供个人信息更新和密码修改功能
 */
public interface UserSelfService {

    /**
     * 更新个人信息
     * @param userId 用户ID
     * @param dto 更新信息
     * @return 更新后的用户信息
     */
    UserInfoVO updateInfo(Long userId, UserUpdateDTO dto);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param dto 密码信息
     */
    void updatePassword(Long userId, PasswordUpdateDTO dto);
}