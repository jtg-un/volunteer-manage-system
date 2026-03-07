package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityAuditDTO;
import com.vms.common.dto.ActivityPublishDTO;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.dto.ActivityStatusDTO;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.common.vo.MyActivityListVO;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 发布活动
     * @param orgId 组织ID
     * @param dto 发布DTO
     * @return 活动ID
     */
    Long publish(Long orgId, ActivityPublishDTO dto);

    /**
     * 分页查询活动列表
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ActivityListVO> list(ActivityQueryDTO query);

    /**
     * 获取活动详情
     * @param activityId 活动ID
     * @return 详情
     */
    ActivityDetailVO getDetail(Long activityId);

    /**
     * 管理员审核活动
     * @param dto 审核DTO
     * @param auditorId 审核人ID
     */
    void audit(ActivityAuditDTO dto, Long auditorId);

    /**
     * 管理员获取待审核活动列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    Page<ActivityListVO> listPending(int page, int size, Integer status);

    /**
     * 组织更新活动状态
     * @param dto 状态DTO
     * @param orgId 组织ID
     */
    void updateStatus(ActivityStatusDTO dto, Long orgId);

    /**
     * 组织获取自己的活动列表
     * @param orgId 组织ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<MyActivityListVO> listByOrg(Long orgId, int page, int size);
}