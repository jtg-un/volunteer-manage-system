package com.vms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.OrgAuditDTO;
import com.vms.common.dto.OrgUpdateDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.OrgDetailVO;
import com.vms.common.vo.OrgListVO;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.SysUser;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.SysUserMapper;
import com.vms.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 组织服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Page<OrgListVO> getPendingList(int page, int size, Integer auditStatus) {
        Page<Organization> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(Organization::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Organization::getOrgId);

        Page<Organization> orgPage = organizationMapper.selectPage(pageParam, wrapper);

        // 转换为 VO
        Page<OrgListVO> voPage = new Page<>(orgPage.getCurrent(), orgPage.getSize(), orgPage.getTotal());
        voPage.setRecords(orgPage.getRecords().stream().map(org -> {
            OrgListVO vo = new OrgListVO();
            vo.setOrgId(org.getOrgId());
            vo.setOrgName(org.getOrgName());
            vo.setUnitType(org.getUnitType());
            vo.setContactPerson(org.getContactPerson());
            vo.setContactPhone(org.getContactPhone());
            vo.setAuditStatus(org.getAuditStatus());
            vo.setCreateTime(org.getAuditTime()); // 暂用审核时间，后续可加创建时间字段

            // 查询用户名
            SysUser user = sysUserMapper.selectById(org.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public OrgDetailVO getOrgDetail(Long orgId) {
        Organization org = organizationMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "组织不存在");
        }

        OrgDetailVO vo = convertToDetailVO(org);

        // 查询用户名
        SysUser user = sysUserMapper.selectById(org.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditOrg(Long auditorId, OrgAuditDTO dto) {
        Organization org = organizationMapper.selectById(dto.getOrgId());
        if (org == null) {
            throw new BusinessException(404, "组织不存在");
        }

        if (org.getAuditStatus() != 0) {
            throw new BusinessException(400, "该组织已审核，无法重复审核");
        }

        if (dto.getAuditStatus() == 2 && (dto.getRejectReason() == null || dto.getRejectReason().isBlank())) {
            throw new BusinessException(400, "拒绝时必须填写拒绝原因");
        }

        // 更新组织审核状态
        LambdaUpdateWrapper<Organization> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Organization::getOrgId, dto.getOrgId())
                .set(Organization::getAuditStatus, dto.getAuditStatus())
                .set(Organization::getAuditTime, LocalDateTime.now())
                .set(Organization::getAuditorId, auditorId)
                .set(dto.getRejectReason() != null, Organization::getRejectReason, dto.getRejectReason());

        organizationMapper.update(null, updateWrapper);

        // 如果审核通过，更新用户状态为正常
        if (dto.getAuditStatus() == 1) {
            LambdaUpdateWrapper<SysUser> userUpdate = new LambdaUpdateWrapper<>();
            userUpdate.eq(SysUser::getUserId, org.getUserId())
                    .set(SysUser::getStatus, 1);
            sysUserMapper.update(null, userUpdate);
        }

        log.info("组织审核完成: orgId={}, status={}, auditorId={}", dto.getOrgId(), dto.getAuditStatus(), auditorId);
    }

    @Override
    public OrgDetailVO getMyOrg(Long userId) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(wrapper);

        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }

        OrgDetailVO vo = convertToDetailVO(org);

        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrg(Long userId, OrgUpdateDTO dto) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(wrapper);

        if (org == null) {
            throw new BusinessException(404, "组织信息不存在");
        }

        if (org.getAuditStatus() != 1) {
            throw new BusinessException(400, "审核通过后才能修改信息");
        }

        // 更新组织信息
        Organization updateOrg = new Organization();
        updateOrg.setOrgId(org.getOrgId());
        updateOrg.setOrgName(dto.getOrgName());
        updateOrg.setUnitType(dto.getUnitType());
        updateOrg.setRegionCode(dto.getRegionCode());
        updateOrg.setContactPerson(dto.getContactPerson());
        updateOrg.setContactPhone(dto.getContactPhone());
        updateOrg.setAddress(dto.getAddress());
        updateOrg.setIntro(dto.getIntro());
        if (dto.getFoundDate() != null && !dto.getFoundDate().isBlank()) {
            updateOrg.setFoundDate(LocalDate.parse(dto.getFoundDate()));
        }

        organizationMapper.updateById(updateOrg);

        log.info("组织信息更新成功: userId={}", userId);
    }

    private OrgDetailVO convertToDetailVO(Organization org) {
        OrgDetailVO vo = new OrgDetailVO();
        vo.setOrgId(org.getOrgId());
        vo.setUserId(org.getUserId());
        vo.setOrgName(org.getOrgName());
        vo.setOrgCode(org.getOrgCode());
        vo.setUnitType(org.getUnitType());
        vo.setRegionCode(org.getRegionCode());
        vo.setFoundDate(org.getFoundDate());
        vo.setContactPerson(org.getContactPerson());
        vo.setContactPhone(org.getContactPhone());
        vo.setAddress(org.getAddress());
        vo.setIntro(org.getIntro());
        vo.setAuditStatus(org.getAuditStatus());
        vo.setAuditTime(org.getAuditTime());
        vo.setRejectReason(org.getRejectReason());
        return vo;
    }
}