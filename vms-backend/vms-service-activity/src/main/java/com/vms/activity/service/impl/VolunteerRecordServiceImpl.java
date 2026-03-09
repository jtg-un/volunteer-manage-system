package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.vo.VolunteerRecordVO;
import com.vms.activity.service.VolunteerRecordService;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 志愿者时长记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerRecordServiceImpl implements VolunteerRecordService {

    private final VolunteerRecordMapper recordMapper;
    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;

    @Override
    public Page<VolunteerRecordVO> listRecords(Long userId, int page, int size) {
        // 查询用户的报名ID列表
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                  .eq(Registration::getRegStatus, 1); // 只查询已通过的报名
        List<Registration> registrations = registrationMapper.selectList(regWrapper);

        if (registrations.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> regIds = registrations.stream()
                .map(Registration::getRegId)
                .collect(Collectors.toList());
        Map<Long, Long> regToActivityMap = registrations.stream()
                .collect(Collectors.toMap(Registration::getRegId, Registration::getActivityId));

        // 分页查询时长记录
        Page<VolunteerRecord> recordPage = new Page<>(page, size);
        LambdaQueryWrapper<VolunteerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(VolunteerRecord::getRegId, regIds)
               .orderByDesc(VolunteerRecord::getAuditTime);
        Page<VolunteerRecord> result = recordMapper.selectPage(recordPage, wrapper);

        // 查询活动信息
        List<Long> activityIds = registrations.stream()
                .map(Registration::getActivityId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<Activity> actWrapper = new LambdaQueryWrapper<>();
        actWrapper.in(Activity::getActivityId, activityIds);
        List<Activity> activities = activityMapper.selectList(actWrapper);
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, a -> a));

        // 转换
        Page<VolunteerRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<VolunteerRecordVO> voList = result.getRecords().stream()
                .map(record -> {
                    VolunteerRecordVO vo = new VolunteerRecordVO();
                    vo.setRecordId(record.getRecordId());
                    vo.setHours(record.getHours());
                    vo.setPoints(record.getPoints());
                    vo.setAuditTime(record.getAuditTime());

                    Long activityId = regToActivityMap.get(record.getRegId());
                    if (activityId != null) {
                        Activity activity = activityMap.get(activityId);
                        if (activity != null) {
                            vo.setActivityTitle(activity.getTitle());
                            vo.setActivityStartTime(activity.getStartTime());
                            vo.setActivityEndTime(activity.getEndTime());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}