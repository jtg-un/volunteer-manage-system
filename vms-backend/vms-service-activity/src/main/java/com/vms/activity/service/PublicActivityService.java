package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;

/**
 * 公开活动服务接口
 * 提供无需认证的活动查询功能
 */
public interface PublicActivityService {

    /**
     * 分页查询活动列表
     * @param query 查询条件
     * @return 活动列表
     */
    Page<ActivityListVO> list(ActivityQueryDTO query);

    /**
     * 获取活动详情
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVO getDetail(Long activityId);
}