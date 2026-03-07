package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新DTO
 */
@Data
public class UserStatusDTO {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 状态: 0禁用 1正常 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}