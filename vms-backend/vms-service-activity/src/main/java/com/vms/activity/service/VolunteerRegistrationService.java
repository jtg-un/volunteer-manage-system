package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.RegistrationDTO;
import com.vms.common.vo.MyRegistrationVO;
import com.vms.common.vo.VolunteerStatsVO;

/**
 * 志愿者报名服务接口
 */
public interface VolunteerRegistrationService {

    /**
     * 报名活动
     * @param userId 志愿者ID
     * @param dto 报名信息
     * @return 报名ID
     */
    Long register(Long userId, RegistrationDTO dto);

    /**
     * 取消报名
     * @param userId 志愿者ID
     * @param regId 报名ID
     */
    void cancel(Long userId, Long regId);

    /**
     * 获取我的报名列表
     * @param userId 志愿者ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<MyRegistrationVO> listMyRegistrations(Long userId, int page, int size);

    /**
     * 获取志愿者统计数据
     * @param userId 志愿者ID
     * @return 统计数据
     */
    VolunteerStatsVO getStats(Long userId);
}