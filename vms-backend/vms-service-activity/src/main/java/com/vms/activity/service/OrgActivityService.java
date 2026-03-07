package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.vo.MyActivityListVO;

/**
 * 组织活动服务接口
 * 提供组织相关的活动管理功能
 */
public interface OrgActivityService {

    /**
     * 发布活动
     * @param orgId 组织ID
     * @param dto 发布信息
     * @return 活动ID
     */
    Long publish(Long orgId, ActivityPublishDTO dto);

    /**
     * 获取组织的活动列表
     * @param orgId 组织ID
     * @param page 页码
     * @param size 每页数量
     * @return 活动列表
     */
    Page<MyActivityListVO> listByOrg(Long orgId, int page, int size);

    /**
     * 更新活动状态
     * @param dto 状态信息
     * @param orgId 组织ID
     */
    void updateStatus(ActivityStatusDTO dto, Long orgId);
}