package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.MyRegistrationVO;
import com.vms.activity.service.CheckinService;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签到签退服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final CheckinLogMapper checkinLogMapper;
    private final ActivityPositionMapper positionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long userId, Long regId) {
        Registration registration = validateRegistration(userId, regId);

        // 检查活动状态
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != 1) {
            throw new BusinessException(400, "活动未开始或已结束，无法签到");
        }

        // 检查是否已签到
        LambdaQueryWrapper<CheckinLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinLog::getRegId, regId)
               .eq(CheckinLog::getCheckType, 0);
        if (checkinLogMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "您已签到，请勿重复签到");
        }

        // 创建签到记录
        CheckinLog checkinLog = new CheckinLog();
        checkinLog.setRegId(regId);
        checkinLog.setCheckType(0); // 签到
        checkinLogMapper.insert(checkinLog);

        log.info("签到成功: userId={}, regId={}", userId, regId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long userId, Long regId) {
        Registration registration = validateRegistration(userId, regId);

        // 检查活动状态
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (activity.getStatus() != 1 && activity.getStatus() != 2) {
            throw new BusinessException(400, "活动未运行或已结束，无法签退");
        }

        // 检查是否已签到
        LambdaQueryWrapper<CheckinLog> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(CheckinLog::getRegId, regId)
                 .eq(CheckinLog::getCheckType, 0);
        if (checkinLogMapper.selectCount(inWrapper) == 0) {
            throw new BusinessException(400, "请先签到后再签退");
        }

        // 检查是否已签退
        LambdaQueryWrapper<CheckinLog> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(CheckinLog::getRegId, regId)
                  .eq(CheckinLog::getCheckType, 1);
        if (checkinLogMapper.selectCount(outWrapper) > 0) {
            throw new BusinessException(400, "您已签退，请勿重复签退");
        }

        // 创建签退记录
        CheckinLog checkinLog = new CheckinLog();
        checkinLog.setRegId(regId);
        checkinLog.setCheckType(1); // 签退
        checkinLogMapper.insert(checkinLog);

        log.info("签退成功: userId={}, regId={}", userId, regId);
    }

    @Override
    public MyRegistrationVO getCheckinStatus(Long userId, Long regId) {
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看该报名信息");
        }

        MyRegistrationVO vo = new MyRegistrationVO();
        vo.setRegId(regId);
        vo.setRegStatus(registration.getRegStatus());

        // 查询签到签退记录
        LambdaQueryWrapper<CheckinLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinLog::getRegId, regId)
               .orderByAsc(CheckinLog::getCheckTime);
        List<CheckinLog> logs = checkinLogMapper.selectList(wrapper);

        for (CheckinLog log : logs) {
            if (log.getCheckType() == 0) {
                vo.setCheckedIn(true);
                vo.setCheckInTime(log.getCheckTime());
            } else if (log.getCheckType() == 1) {
                vo.setCheckedOut(true);
                vo.setCheckOutTime(log.getCheckTime());
            }
        }

        return vo;
    }

    /**
     * 验证报名记录
     */
    private Registration validateRegistration(Long userId, Long regId) {
        Registration registration = registrationMapper.selectById(regId);
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        if (!registration.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能操作自己的报名");
        }
        if (registration.getRegStatus() != 1) {
            throw new BusinessException(400, "报名未通过审核，无法签到/签退");
        }
        return registration;
    }
}