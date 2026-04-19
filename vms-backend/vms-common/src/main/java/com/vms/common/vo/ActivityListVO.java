package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动列表VO
 */
@Data
public class ActivityListVO {

    /** 活动ID */
    private Long activityId;

    /** 项目编号 */
    private String projectCode;

    /** 项目标题 */
    private String title;

    /** 服务类别 */
    private String categoryId;

    /** 服务类别名称 */
    private String categoryName;

    /** 项目属地编码 */
    private String regionCode;

    /** 项目属地名称 */
    private String regionName;

    /** 服务对象 */
    private String targetAudience;

    /** 服务对象名称 */
    private String targetAudienceName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 拒绝原因 */
    private String rejectReason;

    /** 发起组织ID */
    private Long orgId;

    /** 发起组织名称 */
    private String orgName;

    /** 封面图片URL */
    private String coverImageUrl;

    /** 总计划人数（所有岗位之和） */
    private Integer totalPlanCount;

    /** 总已报名人数 */
    private Integer totalCurrentCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}