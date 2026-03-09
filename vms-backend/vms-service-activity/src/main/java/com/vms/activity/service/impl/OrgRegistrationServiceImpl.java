package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.HoursConfirmDTO;
import com.vms.common.dto.RegistrationAuditDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.RegistrationListVO;
import com.vms.activity.service.OrgRegistrationService;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织报名管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgRegistrationServiceImpl implements OrgRegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final ActivityPositionMapper positionMapper;
    private final SysUserMapper userMapper;
    private final CheckinLogMapper checkinLogMapper;
    private final VolunteerRecordMapper recordMapper;

    @Override
    public Page<RegistrationListVO> listRegistrations(Long orgId, Long activityId, Integer status, int page, int size) {
        // 验证活动归属
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (!activity.getOrgId().equals(orgId)) {
            throw new BusinessException(403, "只能查看自己活动的报名");
        }

        // 查询报名列表
        Page<Registration> regPage = new Page<>(page, size);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getActivityId, activityId);
        if (status != null) {
            wrapper.eq(Registration::getRegStatus, status);
        }
        wrapper.orderByDesc(Registration::getCreateTime);
        Page<Registration> result = registrationMapper.selectPage(regPage, wrapper);

        return convertToVOPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long orgId, RegistrationAuditDTO dto) {
        Registration registration = registrationMapper.selectById(dto.getRegId());
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || !activity.getOrgId().equals(orgId)) {
            throw new BusinessException(403, "只能审核自己活动的报名");
        }

        // 验证状态
        if (registration.getRegStatus() != 0) {
            throw new BusinessException(400, "该报名已处理，无法重复审核");
        }

        // 验证审核状态值
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BusinessException(400, "审核状态无效");
        }

        // 更新报名状态
        registration.setRegStatus(dto.getStatus());
        registrationMapper.updateById(registration);

        // 如果拒绝，需要减少岗位人数
        if (dto.getStatus() == 2) {
            ActivityPosition position = positionMapper.selectById(registration.getPosId());
            if (position != null && position.getCurrentCount() > 0) {
                position.setCurrentCount(position.getCurrentCount() - 1);
                positionMapper.updateById(position);
            }
        }

        log.info("审核报名完成: regId={}, status={}", dto.getRegId(), dto.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmHours(Long orgId, HoursConfirmDTO dto) {
        Registration registration = registrationMapper.selectById(dto.getRegId());
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || !activity.getOrgId().equals(orgId)) {
            throw new BusinessException(403, "只能操作自己活动的报名");
        }

        // 验证报名状态
        if (registration.getRegStatus() != 1) {
            throw new BusinessException(400, "只有已通过的报名可以发放时长");
        }

        // 检查是否已发放
        LambdaQueryWrapper<VolunteerRecord> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(VolunteerRecord::getRegId, dto.getRegId());
        if (recordMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(400, "该报名已发放时长");
        }

        // 计算积分（默认按每小时10积分计算）
        Integer points = dto.getPoints();
        if (points == null) {
            points = dto.getHours().multiply(new BigDecimal("10")).intValue();
        }

        // 创建时长记录
        VolunteerRecord record = new VolunteerRecord();
        record.setRegId(dto.getRegId());
        record.setHours(dto.getHours());
        record.setPoints(points);
        record.setAuditorId(orgId);
        recordMapper.insert(record);

        log.info("时长发放成功: regId={}, hours={}, points={}", dto.getRegId(), dto.getHours(), points);
    }

    /**
     * 转换为 VO 分页结果
     */
    private Page<RegistrationListVO> convertToVOPage(Page<Registration> result) {
        Page<RegistrationListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        if (result.getRecords().isEmpty()) {
            return voPage;
        }

        // 批量查询用户信息
        List<Long> userIds = result.getRecords().stream()
                .map(Registration::getUserId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(SysUser::getUserId, userIds);
        List<SysUser> users = userMapper.selectList(userWrapper);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        // 批量查询活动信息
        List<Long> activityIds = result.getRecords().stream()
                .map(Registration::getActivityId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<Activity> actWrapper = new LambdaQueryWrapper<>();
        actWrapper.in(Activity::getActivityId, activityIds);
        List<Activity> activities = activityMapper.selectList(actWrapper);
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, a -> a));

        // 批量查询岗位信息
        List<Long> posIds = result.getRecords().stream()
                .map(Registration::getPosId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, ActivityPosition> posMap;
        if (!posIds.isEmpty()) {
            LambdaQueryWrapper<ActivityPosition> posWrapper = new LambdaQueryWrapper<>();
            posWrapper.in(ActivityPosition::getPosId, posIds);
            posMap = positionMapper.selectList(posWrapper).stream()
                    .collect(Collectors.toMap(ActivityPosition::getPosId, p -> p));
        } else {
            posMap = Map.of();
        }

        // 批量查询签到记录
        List<Long> regIds = result.getRecords().stream()
                .map(Registration::getRegId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<CheckinLog> checkinWrapper = new LambdaQueryWrapper<>();
        checkinWrapper.in(CheckinLog::getRegId, regIds);
        List<CheckinLog> checkinLogs = checkinLogMapper.selectList(checkinWrapper);

        // 按报名ID分组签到记录
        Map<Long, List<CheckinLog>> checkinMap = checkinLogs.stream()
                .collect(Collectors.groupingBy(CheckinLog::getRegId));

        // 批量查询时长记录
        LambdaQueryWrapper<VolunteerRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(VolunteerRecord::getRegId, regIds);
        List<VolunteerRecord> volunteerRecords = recordMapper.selectList(recordWrapper);
        Map<Long, VolunteerRecord> recordMap = volunteerRecords.stream()
                .collect(Collectors.toMap(VolunteerRecord::getRegId, r -> r));

        // 转换
        List<RegistrationListVO> voList = result.getRecords().stream()
                .map(reg -> {
                    RegistrationListVO vo = new RegistrationListVO();
                    vo.setRegId(reg.getRegId());
                    vo.setUserId(reg.getUserId());
                    vo.setActivityId(reg.getActivityId());
                    vo.setPosId(reg.getPosId());
                    vo.setRegStatus(reg.getRegStatus());
                    vo.setStatusName(getRegStatusName(reg.getRegStatus()));
                    vo.setCreateTime(reg.getCreateTime());

                    SysUser user = userMap.get(reg.getUserId());
                    if (user != null) {
                        vo.setRealName(user.getRealName());
                        vo.setPhone(user.getPhone());
                        vo.setAvatarUrl(user.getAvatarUrl());
                    }

                    Activity activity = activityMap.get(reg.getActivityId());
                    if (activity != null) {
                        vo.setActivityTitle(activity.getTitle());
                    }

                    ActivityPosition pos = posMap.get(reg.getPosId());
                    if (pos != null) {
                        vo.setPosName(pos.getPosName());
                    }

                    // 签到状态
                    List<CheckinLog> logs = checkinMap.get(reg.getRegId());
                    if (logs != null) {
                        for (CheckinLog checkinLog : logs) {
                            if (checkinLog.getCheckType() == 0) {
                                vo.setCheckedIn(true);
                            } else if (checkinLog.getCheckType() == 1) {
                                vo.setCheckedOut(true);
                            }
                        }
                    }

                    // 时长记录
                    VolunteerRecord record = recordMap.get(reg.getRegId());
                    if (record != null) {
                        vo.setHoursIssued(true);
                        vo.setHours(record.getHours() != null ? record.getHours().toString() : null);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    private String getRegStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已取消";
            default -> "未知";
        };
    }
}