package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报名审核DTO
 */
@Data
public class RegistrationAuditDTO {

    /** 报名ID */
    @NotNull(message = "报名ID不能为空")
    private Long regId;

    /** 审核状态: 1通过 2拒绝 */
    @NotNull(message = "审核状态不能为空")
    private Integer status;
}