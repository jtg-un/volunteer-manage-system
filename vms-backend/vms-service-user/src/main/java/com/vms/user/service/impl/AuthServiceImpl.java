package com.vms.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.dto.LoginDTO;
import com.vms.common.dto.OrgRegisterDTO;
import com.vms.common.dto.VolunteerRegisterDTO;
import com.vms.common.vo.LoginVO;
import com.vms.common.vo.UserInfoVO;
import com.vms.common.exception.BusinessException;
import com.vms.common.utils.JwtUtils;
import com.vms.common.utils.PasswordUtils;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.SysUser;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.SysUserMapper;
import com.vms.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;

    @Override
    public void registerVolunteer(VolunteerRegisterDTO dto) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(0); // 志愿者
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1); // 正常状态
        sysUserMapper.insert(user);

        log.info("志愿者注册成功: {}", dto.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerOrganization(OrgRegisterDTO dto) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, dto.getUsername());
        if (sysUserMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 创建用户账号（角色为组织负责人）
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getContactPerson());
        user.setRole(1); // 组织负责人
        user.setPhone(dto.getContactPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(0); // 待审核，禁止登录
        sysUserMapper.insert(user);

        // 创建组织信息
        Organization org = new Organization();
        org.setUserId(user.getUserId());
        org.setOrgName(dto.getOrgName());
        org.setUnitType(dto.getUnitType());
        org.setRegionCode(dto.getRegionCode());
        org.setFoundDate(dto.getFoundDate());
        org.setContactPerson(dto.getContactPerson());
        org.setContactPhone(dto.getContactPhone());
        org.setAddress(dto.getAddress());
        org.setIntro(dto.getIntro());
        org.setAuditStatus(0); // 待审核
        organizationMapper.insert(org);

        log.info("组织注册成功，待审核: {} - {}", dto.getUsername(), dto.getOrgName());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 0) {
            // 如果是组织负责人，检查审核状态
            if (user.getRole() == 1) {
                LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
                orgWrapper.eq(Organization::getUserId, user.getUserId());
                Organization org = organizationMapper.selectOne(orgWrapper);
                if (org != null && org.getAuditStatus() == 0) {
                    throw new BusinessException(403, "组织账号待审核，请等待管理员审核通过");
                }
                if (org != null && org.getAuditStatus() == 2) {
                    throw new BusinessException(403, "组织审核未通过，原因：" + (org.getRejectReason() != null ? org.getRejectReason() : "无"));
                }
            }
            throw new BusinessException(403, "账号已被禁用");
        }

        // 生成 Token
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());

        log.info("用户登录成功: {}", user.getUsername());
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());

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