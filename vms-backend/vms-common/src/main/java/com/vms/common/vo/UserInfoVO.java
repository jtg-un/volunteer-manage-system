package com.vms.common.vo;

import lombok.Data;

/**
 * 用户信息 VO
 */
@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private String realName;

    /** 角色: 0志愿者 1组织负责人 2系统管理员 */
    private Integer role;

    private String phone;

    private String email;

    private String avatarUrl;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 组织ID（仅组织负责人） */
    private Long orgId;

    /** 组织名称（仅组织负责人） */
    private String orgName;

    /** 审核状态: 0待审核 1通过 2拒绝（仅组织负责人） */
    private Integer auditStatus;

    /** 拒绝原因（仅组织负责人） */
    private String rejectReason;
}