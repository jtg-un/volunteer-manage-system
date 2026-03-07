package com.vms.activity.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.common.vo.MyActivityListVO;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.ActivityPosition;
import com.vms.repository.entity.Organization;
import com.vms.repository.entity.Registration;
import com.vms.repository.entity.SysDict;
import com.vms.repository.entity.SysRegion;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.ActivityPositionMapper;
import com.vms.repository.mapper.OrganizationMapper;
import com.vms.repository.mapper.RegistrationMapper;
import com.vms.repository.mapper.SysDictMapper;
import com.vms.repository.mapper.SysRegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 活动服务公共辅助类
 * 提供VO转换、字典查询、项目编号生成等公共方法
 */
@Component
@RequiredArgsConstructor
public class ActivitySupport {

    private final ActivityMapper activityMapper;
    private final ActivityPositionMapper positionMapper;
    private final OrganizationMapper organizationMapper;
    private final SysDictMapper dictMapper;
    private final SysRegionMapper regionMapper;
    private final RegistrationMapper registrationMapper;

    private static final String SERVICE_CATEGORY = "service_category";

    // ==================== 状态相关 ====================

    /**
     * 根据状态码获取状态名称
     */
    public String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待启动";
            case 1 -> "运行中";
            case 2 -> "已结项";
            case 3 -> "待审核";
            case 4 -> "已拒绝";
            default -> "未知";
        };
    }

    // ==================== 项目编号生成 ====================

    /**
     * 生成项目编号 (P + 年月日 + 4位序号)
     */
    public String generateProjectCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

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

    // ==================== 字典和地区查询 ====================

    /**
     * 获取字典值
     */
    public String getDictValue(String dictType, String dictKey) {
        if (dictKey == null) return null;
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType)
               .eq(SysDict::getDictKey, dictKey);
        SysDict dict = dictMapper.selectOne(wrapper);
        return dict != null ? dict.getDictValue() : null;
    }

    /**
     * 获取服务类别名称
     */
    public String getCategoryName(String categoryId) {
        return getDictValue(SERVICE_CATEGORY, categoryId);
    }

    /**
     * 获取地区名称
     */
    public String getRegionName(String regionCode) {
        if (regionCode == null) return null;
        SysRegion region = regionMapper.selectById(regionCode);
        return region != null ? region.getRegionName() : null;
    }

    /**
     * 获取组织名称
     */
    public String getOrgName(Long orgId) {
        if (orgId == null) return null;
        Organization org = organizationMapper.selectById(orgId);
        return org != null ? org.getOrgName() : null;
    }

    // ==================== 岗位统计 ====================

    /**
     * 计算岗位计划人数总和
     */
    public int calculateTotalPlanCount(Long activityId) {
        List<ActivityPosition> positions = getPositions(activityId);
        return positions.stream().mapToInt(ActivityPosition::getPlanCount).sum();
    }

    /**
     * 计算岗位当前人数总和
     */
    public int calculateTotalCurrentCount(Long activityId) {
        List<ActivityPosition> positions = getPositions(activityId);
        return positions.stream().mapToInt(ActivityPosition::getCurrentCount).sum();
    }

    /**
     * 获取活动的岗位列表
     */
    public List<ActivityPosition> getPositions(Long activityId) {
        LambdaQueryWrapper<ActivityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityPosition::getActivityId, activityId);
        return positionMapper.selectList(wrapper);
    }

    /**
     * 获取待审核报名数
     */
    public int getPendingRegCount(Long activityId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getActivityId, activityId)
               .eq(Registration::getRegStatus, 0);
        Long count = registrationMapper.selectCount(wrapper);
        return count.intValue();
    }

    // ==================== VO 转换 ====================

    /**
     * 转换为 ActivityListVO
     */
    public ActivityListVO toListVO(Activity activity) {
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

        // 填充关联数据
        vo.setCategoryName(getCategoryName(activity.getCategoryId()));
        vo.setRegionName(getRegionName(activity.getRegionCode()));
        vo.setOrgName(getOrgName(activity.getOrgId()));

        // 岗位统计
        vo.setTotalPlanCount(calculateTotalPlanCount(activity.getActivityId()));
        vo.setTotalCurrentCount(calculateTotalCurrentCount(activity.getActivityId()));

        return vo;
    }

    /**
     * 转换为 MyActivityListVO
     */
    public MyActivityListVO toMyActivityListVO(Activity activity) {
        MyActivityListVO vo = new MyActivityListVO();
        vo.setActivityId(activity.getActivityId());
        vo.setProjectCode(activity.getProjectCode());
        vo.setTitle(activity.getTitle());
        vo.setCategoryId(activity.getCategoryId());
        vo.setCategoryName(getCategoryName(activity.getCategoryId()));
        vo.setRegionCode(activity.getRegionCode());
        vo.setRegionName(getRegionName(activity.getRegionCode()));
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());
        vo.setStatusName(getStatusName(activity.getStatus()));
        vo.setRejectReason(activity.getRejectReason());
        vo.setCreateTime(activity.getCreateTime());

        // 岗位统计
        vo.setTotalPlanCount(calculateTotalPlanCount(activity.getActivityId()));
        vo.setTotalCurrentCount(calculateTotalCurrentCount(activity.getActivityId()));

        // 待审核报名数
        vo.setPendingRegCount(getPendingRegCount(activity.getActivityId()));

        return vo;
    }

    /**
     * 转换为 ActivityDetailVO
     */
    public ActivityDetailVO toDetailVO(Activity activity) {
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

        // 填充关联数据
        vo.setCategoryName(getCategoryName(activity.getCategoryId()));
        vo.setRegionName(getRegionName(activity.getRegionCode()));
        vo.setOrgName(getOrgName(activity.getOrgId()));

        // 岗位列表
        List<ActivityPosition> positions = getPositions(activity.getActivityId());
        List<ActivityDetailVO.PositionVO> posVOList = positions.stream()
                .map(this::toPositionVO)
                .toList();
        vo.setPositions(posVOList);

        return vo;
    }

    /**
     * 转换岗位为 PositionVO
     */
    private ActivityDetailVO.PositionVO toPositionVO(ActivityPosition pos) {
        ActivityDetailVO.PositionVO vo = new ActivityDetailVO.PositionVO();
        vo.setPosId(pos.getPosId());
        vo.setPosName(pos.getPosName());
        vo.setPlanCount(pos.getPlanCount());
        vo.setCurrentCount(pos.getCurrentCount());
        vo.setRemainCount(pos.getPlanCount() - pos.getCurrentCount());
        return vo;
    }
}