package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织列表 VO
 */
@Data
public class OrgListVO {

    private Long orgId;

    /** 用户名 */
    private String username;

    /** 队伍全称 */
    private String orgName;

    /** 单位类型 */
    private String unitType;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 审核状态: 0待审核 1通过 2拒绝 */
    private Integer auditStatus;

    /** 创建时间 */
    private LocalDateTime createTime;
}