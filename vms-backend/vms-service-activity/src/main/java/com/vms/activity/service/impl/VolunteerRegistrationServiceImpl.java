package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.RegistrationDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.MyRegistrationVO;
import com.vms.common.vo.VolunteerStatsVO;
import com.vms.activity.service.VolunteerRegistrationService;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 志愿者报名服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerRegistrationServiceImpl implements VolunteerRegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final ActivityPositionMapper positionMapper;
    private final CheckinLogMapper checkinLogMapper;
    private final VolunteerRecordMapper recordMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(Long userId, RegistrationDTO dto) {
        // 检查活动是否存在且状态为待启动
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != 0) {
            throw new BusinessException(400, "活动已启动或已结束，无法报名");
        }

        // 检查岗位是否存在且有余量
        ActivityPosition position = positionMapper.selectById(dto.getPosId());
        if (position == null || !position.getActivityId().equals(dto.getActivityId())) {
            throw new BusinessException(400, "岗位不存在或不属于该活动");
        }
        if (position.getCurrentCount() >= position.getPlanCount()) {
            throw new BusinessException(400, "该岗位已报满");
        }

        // 检查是否已报名（排除已取消的报名）
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                  .eq(Registration::getActivityId, dto.getActivityId())
                  .ne(Registration::getRegStatus, 3); // 排除已取消的报名
        if (registrationMapper.selectCount(regWrapper) > 0) {
            throw new BusinessException(400, "您已报名该活动，请勿重复报名");
        }

        // 检查是否有已取消的报名记录，如果有则恢复
        LambdaQueryWrapper<Registration> canceledWrapper = new LambdaQueryWrapper<>();
        canceledWrapper.eq(Registration::getUserId, userId)
                       .eq(Registration::getActivityId, dto.getActivityId())
                       .eq(Registration::getRegStatus, 3);
        Registration canceledReg = registrationMapper.selectOne(canceledWrapper);

        if (canceledReg != null) {
            // 恢复已取消的报名记录
            canceledReg.setPosId(dto.getPosId()); // 可能换了岗位
            canceledReg.setRegStatus(0); // 待审核
            registrationMapper.updateById(canceledReg);

            // 更新岗位报名人数
            position.setCurrentCount(position.getCurrentCount() + 1);
            positionMapper.updateById(position);

            log.info("恢复报名成功: userId={}, activityId={}, posId={}", userId, dto.getActivityId(), dto.getPosId());
            return canceledReg.getRegId();
        }

        // 创建新的报名记录
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setActivityId(dto.getActivityId());
        registration.setPosId(dto.getPosId());
        registration.setRegStatus(0); // 待审核
        registrationMapper.insert(registration);

        // 更新岗位报名人数
        position.setCurrentCount(position.getCurrentCount() + 1);
        positionMapper.updateById(position);

        log.info("志愿者报名成功: userId={}, activityId={}, posId={}", userId, dto.getActivityId(), dto.getPosId());
        return registration.getRegId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long regId) {
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能取消自己的报名");
        }
        if (registration.getRegStatus() == 3) {
            throw new BusinessException(400, "报名已取消");
        }
        if (registration.getRegStatus() == 1) {
            // 已通过的报名需要检查是否有签到记录
            LambdaQueryWrapper<CheckinLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CheckinLog::getRegId, regId);
            if (checkinLogMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "已有签到记录，无法取消报名");
            }
        }

        // 更新报名状态
        Integer oldStatus = registration.getRegStatus();
        registration.setRegStatus(3); // 取消
        registrationMapper.updateById(registration);

        // 待审核或已通过状态需要减少岗位人数
        // 已拒绝状态不需要减少，因为组织拒绝时已经减少了
        if (oldStatus == 0 || oldStatus == 1) {
            ActivityPosition position = positionMapper.selectById(registration.getPosId());
            if (position != null && position.getCurrentCount() > 0) {
                position.setCurrentCount(position.getCurrentCount() - 1);
                positionMapper.updateById(position);
            }
        }

        log.info("取消报名成功: regId={}", regId);
    }

    @Override
    public Page<MyRegistrationVO> listMyRegistrations(Long userId, int page, int size) {
        Page<Registration> regPage = new Page<>(page, size);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, userId)
               .orderByDesc(Registration::getCreateTime);
        Page<Registration> result = registrationMapper.selectPage(regPage, wrapper);

        return convertToVOPage(result);
    }

    @Override
    public VolunteerStatsVO getStats(Long userId) {
        VolunteerStatsVO stats = new VolunteerStatsVO();

        // 累计时长和积分
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                  .eq(Registration::getRegStatus, 1); // 只统计已通过的报名
        List<Registration> registrations = registrationMapper.selectList(regWrapper);

        if (registrations.isEmpty()) {
            stats.setTotalHours(BigDecimal.ZERO);
            stats.setTotalPoints(0);
            stats.setActivityCount(0);
            stats.setMonthHours(BigDecimal.ZERO);
            stats.setYearHours(BigDecimal.ZERO);
            return stats;
        }

        List<Long> regIds = registrations.stream()
                .map(Registration::getRegId)
                .collect(Collectors.toList());

        // 查询时长记录
        LambdaQueryWrapper<VolunteerRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(VolunteerRecord::getRegId, regIds);
        List<VolunteerRecord> records = recordMapper.selectList(recordWrapper);

        // 计算累计时长和积分
        BigDecimal totalHours = records.stream()
                .map(VolunteerRecord::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalPoints = records.stream()
                .mapToInt(r -> r.getPoints() != null ? r.getPoints() : 0)
                .sum();

        stats.setTotalHours(totalHours);
        stats.setTotalPoints(totalPoints);
        stats.setActivityCount(registrations.size());

        // 计算本月时长
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);

        BigDecimal monthHours = BigDecimal.ZERO;
        BigDecimal yearHours = BigDecimal.ZERO;

        for (VolunteerRecord record : records) {
            if (record.getAuditTime() != null) {
                if (record.getAuditTime().isAfter(monthStart)) {
                    monthHours = monthHours.add(record.getHours());
                }
                if (record.getAuditTime().isAfter(yearStart)) {
                    yearHours = yearHours.add(record.getHours());
                }
            }
        }

        stats.setMonthHours(monthHours);
        stats.setYearHours(yearHours);

        return stats;
    }

    /**
     * 转换为 VO 分页结果
     */
    private Page<MyRegistrationVO> convertToVOPage(Page<Registration> result) {
        Page<MyRegistrationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        if (result.getRecords().isEmpty()) {
            return voPage;
        }

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
        List<MyRegistrationVO> voList = result.getRecords().stream()
                .map(reg -> {
                    MyRegistrationVO vo = new MyRegistrationVO();
                    vo.setRegId(reg.getRegId());
                    vo.setRegStatus(reg.getRegStatus());
                    vo.setRegStatusName(getRegStatusName(reg.getRegStatus()));
                    vo.setCreateTime(reg.getCreateTime());

                    Activity activity = activityMap.get(reg.getActivityId());
                    if (activity != null) {
                        vo.setActivityId(activity.getActivityId());
                        vo.setActivityTitle(activity.getTitle());
                        vo.setProjectCode(activity.getProjectCode());
                        vo.setActivityStartTime(activity.getStartTime());
                        vo.setActivityEndTime(activity.getEndTime());
                        vo.setActivityStatus(activity.getStatus());
                        vo.setActivityStatusName(getActivityStatusName(activity.getStatus()));
                    }

                    ActivityPosition pos = posMap.get(reg.getPosId());
                    if (pos != null) {
                        vo.setPosName(pos.getPosName());
                    }

                    // 签到状态
                    List<CheckinLog> logs = checkinMap.get(reg.getRegId());
                    if (logs != null) {
                        for (CheckinLog log : logs) {
                            if (log.getCheckType() == 0) {
                                vo.setCheckedIn(true);
                                vo.setCheckInTime(log.getCheckTime());
                            } else if (log.getCheckType() == 1) {
                                vo.setCheckedOut(true);
                                vo.setCheckOutTime(log.getCheckTime());
                            }
                        }
                    }

                    // 时长记录
                    VolunteerRecord record = recordMap.get(reg.getRegId());
                    if (record != null) {
                        vo.setHoursIssued(true);
                        vo.setHours(record.getHours());
                        vo.setPoints(record.getPoints());
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

    private String getActivityStatusName(Integer status) {
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
}