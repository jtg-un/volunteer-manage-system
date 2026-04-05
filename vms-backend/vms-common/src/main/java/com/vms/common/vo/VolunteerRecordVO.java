package com.vms.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿时长记录VO
 */
@Data
public class VolunteerRecordVO {

    private Long recordId;

    /** 活动ID */
    private Long activityId;

    /** 活动标题 */
    private String activityTitle;

    /** 活动开始时间 */
    private LocalDateTime activityStartTime;

    /** 活动结束时间 */
    private LocalDateTime activityEndTime;

    /** 核定小时数 */
    private BigDecimal hours;

    /** 核定积分 */
    private Integer points;

    /** 发放时间 */
    private LocalDateTime auditTime;
}