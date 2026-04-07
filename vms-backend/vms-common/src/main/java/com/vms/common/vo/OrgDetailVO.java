package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 组织详情 VO
 */
@Data
public class OrgDetailVO {

    private Long orgId;

    private Long userId;

    /** 用户名 */
    private String username;

    /** 队伍全称 */
    private String orgName;

    /** 队伍联络编号 */
    private String orgCode;

    /** 单位类型 */
    private String unitType;

    /** 属地编码 */
    private String regionCode;

    /** 属地名称 */
    private String regionName;

    /** 成立日期 */
    private LocalDate foundDate;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 简介 */
    private String intro;

    /** 审核状态: 0待审核 1通过 2拒绝 */
    private Integer auditStatus;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 拒绝原因 */
    private String rejectReason;

    /** 创建时间 */
    private LocalDateTime createTime;
}