package com.vms.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价记录VO
 */
@Data
public class EvaluationVO {

    private Long evalId;

    /** 报名ID */
    private Long regId;

    /** 活动标题 */
    private String activityTitle;

    /** 志愿者姓名 */
    private String volunteerName;

    /** 志愿者手机号 */
    private String volunteerPhone;

    /** 培训评分 */
    private BigDecimal scoreTraining;

    /** 协作评分 */
    private BigDecimal scoreCooperation;

    /** 执行力评分 */
    private BigDecimal scoreExecution;

    /** 综合评分 */
    private BigDecimal avgScore;

    /** 评价内容 */
    private String comment;

    /** 评价时间 */
    private LocalDateTime createTime;

    /** 组织名称 */
    private String orgName;
}