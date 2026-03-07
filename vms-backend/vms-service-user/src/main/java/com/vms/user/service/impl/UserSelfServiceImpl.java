package com.vms.user.service.impl;

import com.vms.common.dto.PasswordUpdateDTO;
import com.vms.common.dto.UserUpdateDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.utils.PasswordUtils;
import com.vms.common.vo.UserInfoVO;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.SysUser;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.SysUserMapper;
import com.vms.user.service.UserSelfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户个人服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSelfServiceImpl implements UserSelfService {

    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final PasswordUtils passwordUtils;

    @Override
    public UserInfoVO updateInfo(Long userId, UserUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新字段
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        sysUserMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", userId);

        return buildUserInfoVO(user);
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证原密码
        if (!passwordUtils.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }

        // 更新密码
        user.setPassword(passwordUtils.encode(dto.getNewPassword()));
        sysUserMapper.updateById(user);
        log.info("密码修改成功: userId={}", userId);
    }

    /**
     * 构建用户信息VO
     */
    private UserInfoVO buildUserInfoVO(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());

        // 如果是组织负责人，查询组织信息
        if (user.getRole() == 1) {
            Organization org = organizationMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Organization>()
                    .eq(Organization::getUserId, user.getUserId())
            );
            if (org != null) {
                vo.setOrgId(org.getOrgId());
                vo.setOrgName(org.getOrgName());
                vo.setAuditStatus(org.getAuditStatus());
                vo.setRejectReason(org.getRejectReason());
            }
        }

        return vo;
    }
}