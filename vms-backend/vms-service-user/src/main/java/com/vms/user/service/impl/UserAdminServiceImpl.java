package com.vms.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.UserStatusDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.UserInfoVO;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.SysUser;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.SysUserMapper;
import com.vms.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;

    @Override
    public Page<UserInfoVO> listUsers(String keyword, Integer role, Integer status, int page, int size) {
        Page<SysUser> userPage = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名、姓名、手机号）
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword)
            );
        }

        // 角色筛选
        if (role != null) {
            wrapper.eq(SysUser::getRole, role);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = sysUserMapper.selectPage(userPage, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public void updateStatus(UserStatusDTO dto) {
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 不允许封禁管理员账号
        if (user.getRole() == 2) {
            throw new BusinessException(403, "不允许修改管理员账号状态");
        }

        // 验证状态值
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException(400, "状态值无效");
        }

        user.setStatus(dto.getStatus());
        sysUserMapper.updateById(user);
        log.info("用户状态更新: userId={}, status={}", dto.getUserId(), dto.getStatus());
    }

    /**
     * 转换为VO分页结果
     */
    private Page<UserInfoVO> convertToVOPage(Page<SysUser> result) {
        Page<UserInfoVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserInfoVO> voList = result.getRecords().stream()
                .map(this::buildUserInfoVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
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
            LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
            orgWrapper.eq(Organization::getUserId, user.getUserId());
            Organization org = organizationMapper.selectOne(orgWrapper);
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