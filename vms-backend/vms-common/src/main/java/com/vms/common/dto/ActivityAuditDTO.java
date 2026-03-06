package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动审核DTO
 */
@Data
public class ActivityAuditDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    private String rejectReason;
}