package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织活动列表VO（含报名统计）
 */
@Data
public class MyActivityListVO {

    private Long activityId;

    private String projectCode;

    private String title;

    private String categoryId;

    private String categoryName;

    private String regionCode;

    private String regionName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String statusName;

    /** 拒绝原因 */
    private String rejectReason;

    private LocalDateTime createTime;

    /** 总计划人数 */
    private Integer totalPlanCount;

    /** 已报名人数 */
    private Integer totalCurrentCount;

    /** 待审核报名数 */
    private Integer pendingRegCount;
}