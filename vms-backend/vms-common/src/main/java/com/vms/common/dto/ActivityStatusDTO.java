package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动状态变更DTO
 */
@Data
public class ActivityStatusDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "目标状态不能为空")
    private Integer status;
}