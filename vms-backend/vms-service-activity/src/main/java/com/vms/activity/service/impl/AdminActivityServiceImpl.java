package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.ActivityListVO;
import com.vms.activity.service.AdminActivityService;
import com.vms.activity.service.support.ActivitySupport;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminActivityServiceImpl implements AdminActivityService {

    private final ActivityMapper activityMapper;
    private final OrganizationMapper organizationMapper;
    private final ActivitySupport support;

    @Override
    public Page<ActivityListVO> listPending(int page, int size, String keyword, String orgName, Integer status) {
        Page<Activity> activityPage = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（活动标题、项目编号）
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(Activity::getTitle, keyword)
                    .or().like(Activity::getProjectCode, keyword)
            );
        }

        // 组织名称筛选
        if (orgName != null && !orgName.isBlank()) {
            // 先查询匹配的组织ID
            LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
            orgWrapper.like(Organization::getOrgName, orgName)
                     .select(Organization::getOrgId);
            List<Long> orgIds = organizationMapper.selectList(orgWrapper)
                    .stream()
                    .map(Organization::getOrgId)
                    .collect(Collectors.toList());

            if (orgIds.isEmpty()) {
                // 没有匹配的组织，返回空结果
                return new Page<>(page, size, 0);
            }
            wrapper.in(Activity::getOrgId, orgIds);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }

        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(activityPage, wrapper);
        return convertToVOPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(ActivityAuditDTO dto, Long auditorId) {
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        if (activity.getStatus() != 3) {
            throw new BusinessException(400, "只能审核待审核状态的活动");
        }

        applyAuditResult(activity, dto);
        activityMapper.updateById(activity);
        log.info("活动审核完成: activityId={}, result={}", dto.getActivityId(), dto.getAuditResult());
    }

    /**
     * 应用审核结果
     */
    private void applyAuditResult(Activity activity, ActivityAuditDTO dto) {
        Integer result = dto.getAuditResult();
        if (result == 1) {
            // 审核通过 -> 待启动
            activity.setStatus(0);
            activity.setRejectReason(null);
        } else if (result == 2) {
            // 审核拒绝
            if (dto.getRejectReason() == null || dto.getRejectReason().isBlank()) {
                throw new BusinessException(400, "拒绝时必须填写拒绝原因");
            }
            activity.setStatus(4);
            activity.setRejectReason(dto.getRejectReason());
        } else {
            throw new BusinessException(400, "无效的审核结果");
        }
    }

    /**
     * 转换为 VO 分页结果
     */
    private Page<ActivityListVO> convertToVOPage(Page<Activity> result) {
        Page<ActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ActivityListVO> voList = result.getRecords().stream()
                .map(support::toListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}