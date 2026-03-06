package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 志愿队伍详情实体
 */
@Data
@TableName("organization")
public class Organization {

    @TableId(type = IdType.AUTO)
    private Long orgId;

    /** 关联负责人账号 */
    private Long userId;

    /** 队伍全称 */
    private String orgName;

    /** 队伍联络编号 */
    private String orgCode;

    /** 单位类型 */
    private String unitType;

    /** 属地编码 */
    private String regionCode;

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

    /** 审核人ID */
    private Long auditorId;

    /** 拒绝原因 */
    private String rejectReason;
}