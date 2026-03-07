package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.MyActivityListVO;
import com.vms.activity.service.OrgActivityService;
import com.vms.activity.service.support.ActivitySupport;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.ActivityPosition;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.ActivityPositionMapper;
import com.vms.repository.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgActivityServiceImpl implements OrgActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityPositionMapper positionMapper;
    private final OrganizationMapper organizationMapper;
    private final ActivitySupport support;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long orgId, ActivityPublishDTO dto) {
        // 验证组织
        Organization org = organizationMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(404, "组织不存在");
        }

        // 验证时间
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }

        // 创建活动
        Activity activity = new Activity();
        activity.setOrgId(orgId);
        activity.setProjectCode(support.generateProjectCode());
        activity.setTitle(dto.getTitle());
        activity.setCategoryId(dto.getCategoryId());
        activity.setRegionCode(dto.getRegionCode());
        activity.setTargetAudience(dto.getTargetAudience());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setStatus(3); // 待审核
        activity.setDescription(dto.getDescription());
        activityMapper.insert(activity);

        // 创建岗位
        createPositions(activity.getActivityId(), dto.getPositions());

        log.info("活动发布成功: {} - {}", activity.getProjectCode(), activity.getTitle());
        return activity.getActivityId();
    }

    @Override
    public Page<MyActivityListVO> listByOrg(Long orgId, int page, int size) {
        Page<Activity> activityPage = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrgId, orgId)
               .orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(activityPage, wrapper);
        return convertToVOPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(ActivityStatusDTO dto, Long orgId) {
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        if (!activity.getOrgId().equals(orgId)) {
            throw new BusinessException(403, "只能操作自己的活动");
        }

        Integer currentStatus = activity.getStatus();
        Integer targetStatus = dto.getStatus();

        // 状态流转校验
        validateStatusTransition(currentStatus, targetStatus);

        activity.setStatus(targetStatus);
        activityMapper.updateById(activity);
        log.info("活动状态变更: activityId={}, {} -> {}", dto.getActivityId(), currentStatus, targetStatus);
    }

    /**
     * 创建活动岗位
     */
    private void createPositions(Long activityId, List<ActivityPublishDTO.PositionDTO> positions) {
        for (ActivityPublishDTO.PositionDTO posDto : positions) {
            ActivityPosition position = new ActivityPosition();
            position.setActivityId(activityId);
            position.setPosName(posDto.getPosName());
            position.setPlanCount(posDto.getPlanCount());
            position.setCurrentCount(0);
            positionMapper.insert(position);
        }
    }

    /**
     * 验证状态流转是否合法
     */
    private void validateStatusTransition(Integer current, Integer target) {
        boolean valid = (current == 0 && target == 1) || (current == 1 && target == 2);
        if (!valid) {
            throw new BusinessException(400, "不允许的状态变更");
        }
    }

    /**
     * 转换为 VO 分页结果
     */
    private Page<MyActivityListVO> convertToVOPage(Page<Activity> result) {
        Page<MyActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<MyActivityListVO> voList = result.getRecords().stream()
                .map(support::toMyActivityListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}