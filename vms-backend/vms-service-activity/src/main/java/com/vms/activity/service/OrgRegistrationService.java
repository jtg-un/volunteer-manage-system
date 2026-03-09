package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.HoursConfirmDTO;
import com.vms.common.dto.RegistrationAuditDTO;
import com.vms.common.vo.RegistrationListVO;

/**
 * 组织报名管理服务接口
 */
public interface OrgRegistrationService {

    /**
     * 获取活动的报名列表
     * @param orgId 组织ID
     * @param activityId 活动ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<RegistrationListVO> listRegistrations(Long orgId, Long activityId, Integer status, int page, int size);

    /**
     * 审核报名
     * @param orgId 组织ID
     * @param dto 审核信息
     */
    void audit(Long orgId, RegistrationAuditDTO dto);

    /**
     * 确认发放时长
     * @param orgId 组织ID
     * @param dto 时长信息
     */
    void confirmHours(Long orgId, HoursConfirmDTO dto);
}