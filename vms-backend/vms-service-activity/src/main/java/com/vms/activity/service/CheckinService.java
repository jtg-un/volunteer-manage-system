package com.vms.activity.service;

import com.vms.common.vo.MyRegistrationVO;

/**
 * 签到签退服务接口
 */
public interface CheckinService {

    /**
     * 签到
     * @param userId 志愿者ID
     * @param regId 报名ID
     */
    void checkIn(Long userId, Long regId);

    /**
     * 签退
     * @param userId 志愿者ID
     * @param regId 报名ID
     */
    void checkOut(Long userId, Long regId);

    /**
     * 获取报名的签到状态
     * @param userId 志愿者ID
     * @param regId 报名ID
     * @return 报名信息（含签到状态）
     */
    MyRegistrationVO getCheckinStatus(Long userId, Long regId);
}