package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.vo.ActivityListVO;

/**
 * 管理员活动服务接口
 * 提供管理员相关的活动审核功能
 */
public interface AdminActivityService {

    /**
     * 获取活动列表（支持状态筛选）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词（活动标题/项目编号）
     * @param orgName 组织名称
     * @param status 状态（可选）
     * @return 活动列表
     */
    Page<ActivityListVO> listPending(int page, int size, String keyword, String orgName, Integer status);

    /**
     * 审核活动
     * @param dto 审核信息
     * @param auditorId 审核人ID
     */
    void audit(ActivityAuditDTO dto, Long auditorId);
}