package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 志愿者报名DTO
 */
@Data
public class RegistrationDTO {

    /** 活动ID */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    /** 岗位ID */
    @NotNull(message = "岗位ID不能为空")
    private Long posId;
}