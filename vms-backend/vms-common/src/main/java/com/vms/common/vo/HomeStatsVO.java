package com.vms.common.vo;

import lombok.Data;

/**
 * 首页统计数据VO
 */
@Data
public class HomeStatsVO {

    /** 志愿者总数 */
    private Long volunteerCount;

    /** 组织总数 */
    private Long orgCount;

    /** 活动总数 */
    private Long activityCount;

    /** 运行中活动数 */
    private Long runningActivityCount;

    /** 累计志愿服务时长 */
    private Long totalHours;

    /** 累计参与人次 */
    private Long totalParticipations;
}