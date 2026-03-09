package com.vms.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的报名VO（志愿者查看自己的报名）
 */
@Data
public class MyRegistrationVO {

    private Long regId;

    /** 活动ID */
    private Long activityId;

    /** 活动标题 */
    private String activityTitle;

    /** 项目编号 */
    private String projectCode;

    /** 服务类别名称 */
    private String categoryName;

    /** 活动地点 */
    private String regionName;

    /** 活动开始时间 */
    private LocalDateTime activityStartTime;

    /** 活动结束时间 */
    private LocalDateTime activityEndTime;

    /** 活动状态 */
    private Integer activityStatus;

    /** 活动状态名称 */
    private String activityStatusName;

    /** 岗位名称 */
    private String posName;

    /** 报名状态: 0待审 1成功 2拒绝 3取消 */
    private Integer regStatus;

    /** 报名状态名称 */
    private String regStatusName;

    /** 报名时间 */
    private LocalDateTime createTime;

    /** 签到状态 */
    private Boolean checkedIn;

    /** 签退状态 */
    private Boolean checkedOut;

    /** 签到时间 */
    private LocalDateTime checkInTime;

    /** 签退时间 */
    private LocalDateTime checkOutTime;

    /** 时长是否已发放 */
    private Boolean hoursIssued;

    /** 核定小时数 */
    private BigDecimal hours;

    /** 核定积分 */
    private Integer points;
}