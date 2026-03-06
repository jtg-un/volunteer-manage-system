package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组织审核 DTO
 */
@Data
public class OrgAuditDTO {

    /** 组织ID */
    @NotNull(message = "组织ID不能为空")
    private Long orgId;

    /** 审核状态: 1通过 2拒绝 */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /** 拒绝原因（拒绝时必填） */
    private String rejectReason;
}