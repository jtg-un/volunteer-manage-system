package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.common.vo.MyActivityListVO;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.ActivityPosition;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.Registration;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.ActivityPositionMapper;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.RegistrationMapper;
import com.vms.repository.mapper.SysDictMapper;
import com.vms.repository.mapper.SysRegionMapper;
import com.vms.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityPositionMapper positionMapper;
    private final OrganizationMapper organizationMapper;
    private final SysDictMapper dictMapper;
    private final SysRegionMapper regionMapper;
    private final RegistrationMapper registrationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long orgId, ActivityPublishDTO dto) {
        // 验证组织是否存在
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
        activity.setProjectCode(generateProjectCode());
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
        for (ActivityPublishDTO.PositionDTO posDto : dto.getPositions()) {
            ActivityPosition position = new ActivityPosition();
            position.setActivityId(activity.getActivityId());
            position.setPosName(posDto.getPosName());
            position.setPlanCount(posDto.getPlanCount());
            position.setCurrentCount(0);
            positionMapper.insert(position);
        }

        log.info("活动发布成功: {} - {}", activity.getProjectCode(), activity.getTitle());
        return activity.getActivityId();
    }

    @Override
    public Page<ActivityListVO> list(ActivityQueryDTO query) {
        Page<Activity> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        // 动态筛选条件
        if (query.getRegionCode() != null && !query.getRegionCode().isEmpty()) {
            // 支持地区前缀匹配（选择省时查询该省所有市的活动）
            wrapper.likeRight(Activity::getRegionCode, query.getRegionCode().substring(0, 2));
        }
        if (query.getCategoryId() != null && !query.getCategoryId().isEmpty()) {
            wrapper.eq(Activity::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Activity::getStatus, query.getStatus());
        } else if (query.getStatusList() != null && !query.getStatusList().isEmpty()) {
            // 支持多状态查询
            wrapper.in(Activity::getStatus, query.getStatusList());
        }
        if (query.getTargetAudience() != null && !query.getTargetAudience().isEmpty()) {
            wrapper.eq(Activity::getTargetAudience, query.getTargetAudience());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Activity::getTitle, query.getKeyword());
        }

        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ActivityListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ActivityDetailVO getDetail(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivityId(activity.getActivityId());
        vo.setProjectCode(activity.getProjectCode());
        vo.setTitle(activity.getTitle());
        vo.setCategoryId(activity.getCategoryId());
        vo.setRegionCode(activity.getRegionCode());
        vo.setTargetAudience(activity.getTargetAudience());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        vo.setStatusName(getStatusName(activity.getStatus()));
        vo.setDescription(activity.getDescription());
        vo.setRejectReason(activity.getRejectReason());
        vo.setOrgId(activity.getOrgId());
        vo.setCreateTime(activity.getCreateTime());

        // 查询服务类别名称
        String categoryName = getDictValue("service_category", activity.getCategoryId());
        vo.setCategoryName(categoryName);

        // 查询地区名称
        String regionName = getRegionName(activity.getRegionCode());
        vo.setRegionName(regionName);

        // 查询组织名称
        Organization org = organizationMapper.selectById(activity.getOrgId());
        if (org != null) {
            vo.setOrgName(org.getOrgName());
        }

        // 查询岗位列表
        LambdaQueryWrapper<ActivityPosition> posWrapper = new LambdaQueryWrapper<>();
        posWrapper.eq(ActivityPosition::getActivityId, activityId);
        List<ActivityPosition> positions = positionMapper.selectList(posWrapper);

        List<ActivityDetailVO.PositionVO> posVOList = positions.stream()
                .map(pos -> {
                    ActivityDetailVO.PositionVO posVO = new ActivityDetailVO.PositionVO();
                    posVO.setPosId(pos.getPosId());
                    posVO.setPosName(pos.getPosName());
                    posVO.setPlanCount(pos.getPlanCount());
                    posVO.setCurrentCount(pos.getCurrentCount());
                    posVO.setRemainCount(pos.getPlanCount() - pos.getCurrentCount());
                    return posVO;
                })
                .collect(Collectors.toList());
        vo.setPositions(posVOList);

        return vo;
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成项目编号 (P + 年月日 + 4位序号)
     */
    private String generateProjectCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查询当天最大编号
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Activity::getProjectCode, "P" + dateStr)
               .orderByDesc(Activity::getProjectCode)
               .last("LIMIT 1");
        Activity lastActivity = activityMapper.selectOne(wrapper);

        int seq = 1;
        if (lastActivity != null && lastActivity.getProjectCode() != null) {
            String lastCode = lastActivity.getProjectCode();
            seq = Integer.parseInt(lastCode.substring(lastCode.length() - 4)) + 1;
        }

        return String.format("P%s%04d", dateStr, seq);
    }

    private ActivityListVO convertToListVO(Activity activity) {
        ActivityListVO vo = new ActivityListVO();
        vo.setActivityId(activity.getActivityId());
        vo.setProjectCode(activity.getProjectCode());
        vo.setTitle(activity.getTitle());
        vo.setCategoryId(activity.getCategoryId());
        vo.setRegionCode(activity.getRegionCode());
        vo.setTargetAudience(activity.getTargetAudience());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        vo.setStatusName(getStatusName(activity.getStatus()));
        vo.setRejectReason(activity.getRejectReason());
        vo.setCreateTime(activity.getCreateTime());

        // 查询服务类别名称
        String categoryName = getDictValue("service_category", activity.getCategoryId());
        vo.setCategoryName(categoryName);

        // 查询地区名称
        String regionName = getRegionName(activity.getRegionCode());
        vo.setRegionName(regionName);

        // 查询组织名称
        Organization org = organizationMapper.selectById(activity.getOrgId());
        if (org != null) {
            vo.setOrgName(org.getOrgName());
        }

        // 查询岗位人数统计
        LambdaQueryWrapper<ActivityPosition> posWrapper = new LambdaQueryWrapper<>();
        posWrapper.eq(ActivityPosition::getActivityId, activity.getActivityId());
        List<ActivityPosition> positions = positionMapper.selectList(posWrapper);

        int totalPlan = 0;
        int totalCurrent = 0;
        for (ActivityPosition pos : positions) {
            totalPlan += pos.getPlanCount();
            totalCurrent += pos.getCurrentCount();
        }
        vo.setTotalPlanCount(totalPlan);
        vo.setTotalCurrentCount(totalCurrent);

        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待启动";
            case 1: return "运行中";
            case 2: return "已结项";
            case 3: return "待审核";
            case 4: return "已拒绝";
            default: return "未知";
        }
    }

    private String getDictValue(String dictType, String dictKey) {
        if (dictKey == null) return null;
        LambdaQueryWrapper<com.vms.repository.entity.SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.vms.repository.entity.SysDict::getDictType, dictType)
               .eq(com.vms.repository.entity.SysDict::getDictKey, dictKey);
        com.vms.repository.entity.SysDict dict = dictMapper.selectOne(wrapper);
        return dict != null ? dict.getDictValue() : null;
    }

    private String getRegionName(String regionCode) {
        if (regionCode == null) return null;
        com.vms.repository.entity.SysRegion region = regionMapper.selectById(regionCode);
        return region != null ? region.getRegionName() : null;
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

        Integer result = dto.getAuditResult();
        if (result == 1) {
            // 审核通过 -> 待启动
            activity.setStatus(0);
            activity.setRejectReason(null);
        } else if (result == 2) {
            // 审核拒绝
            activity.setStatus(4);
            activity.setRejectReason(dto.getRejectReason());
        } else {
            throw new BusinessException(400, "无效的审核结果");
        }

        activityMapper.updateById(activity);
        log.info("活动审核完成: activityId={}, result={}", dto.getActivityId(), result);
    }

    @Override
    public Page<ActivityListVO> listPending(int page, int size, Integer status) {
        Page<Activity> activityPage = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        // 如果不传状态，默认查询所有活动

        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(activityPage, wrapper);

        // 转换为VO
        Page<ActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ActivityListVO> voList = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
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
        if (currentStatus == 0 && targetStatus == 1) {
            // 待启动 -> 运行中
            activity.setStatus(1);
        } else if (currentStatus == 1 && targetStatus == 2) {
            // 运行中 -> 已结项
            activity.setStatus(2);
        } else {
            throw new BusinessException(400, "不允许的状态变更");
        }

        activityMapper.updateById(activity);
        log.info("活动状态变更: activityId={}, {} -> {}", dto.getActivityId(), currentStatus, targetStatus);
    }

    @Override
    public Page<MyActivityListVO> listByOrg(Long orgId, int page, int size) {
        Page<Activity> activityPage = new Page<>(page, size);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getOrgId, orgId)
               .orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(activityPage, wrapper);

        // 转换为VO
        Page<MyActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<MyActivityListVO> voList = result.getRecords().stream()
                .map(this::convertToMyActivityListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    private MyActivityListVO convertToMyActivityListVO(Activity activity) {
        MyActivityListVO vo = new MyActivityListVO();
        vo.setActivityId(activity.getActivityId());
        vo.setProjectCode(activity.getProjectCode());
        vo.setTitle(activity.getTitle());
        vo.setCategoryId(activity.getCategoryId());
        vo.setCategoryName(getDictValue("service_category", activity.getCategoryId()));
        vo.setRegionCode(activity.getRegionCode());
        vo.setRegionName(getRegionName(activity.getRegionCode()));
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        vo.setStatusName(getStatusName(activity.getStatus()));
        vo.setRejectReason(activity.getRejectReason());
        vo.setCreateTime(activity.getCreateTime());

        // 查询岗位人数统计
        LambdaQueryWrapper<ActivityPosition> posWrapper = new LambdaQueryWrapper<>();
        posWrapper.eq(ActivityPosition::getActivityId, activity.getActivityId());
        List<ActivityPosition> positions = positionMapper.selectList(posWrapper);

        int totalPlan = 0;
        int totalCurrent = 0;
        for (ActivityPosition pos : positions) {
            totalPlan += pos.getPlanCount();
            totalCurrent += pos.getCurrentCount();
        }
        vo.setTotalPlanCount(totalPlan);
        vo.setTotalCurrentCount(totalCurrent);

        // 查询待审核报名数
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getActivityId, activity.getActivityId())
                  .eq(Registration::getRegStatus, 0); // 待审核
        Long pendingCount = registrationMapper.selectCount(regWrapper);
        vo.setPendingRegCount(pendingCount.intValue());

        return vo;
    }
}