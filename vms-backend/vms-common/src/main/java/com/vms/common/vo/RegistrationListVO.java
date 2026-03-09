package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名列表VO（组织查看报名者）
 */
@Data
public class RegistrationListVO {

    private Long regId;

    /** 志愿者ID */
    private Long userId;

    /** 志愿者姓名 */
    private String realName;

    /** 志愿者手机号 */
    private String phone;

    /** 志愿者头像 */
    private String avatarUrl;

    /** 活动ID */
    private Long activityId;

    /** 活动标题 */
    private String activityTitle;

    /** 岗位ID */
    private Long posId;

    /** 岗位名称 */
    private String posName;

    /** 报名状态: 0待审 1成功 2拒绝 3取消 */
    private Integer regStatus;

    /** 报名状态名称 */
    private String statusName;

    /** 报名时间 */
    private LocalDateTime createTime;

    /** 签到状态: true已签到 */
    private Boolean checkedIn;

    /** 签退状态: true已签退 */
    private Boolean checkedOut;

    /** 时长是否已发放 */
    private Boolean hoursIssued;

    /** 核定小时数 */
    private String hours;
}