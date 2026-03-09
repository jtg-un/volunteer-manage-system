package com.vms.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 时长确认DTO
 */
@Data
public class HoursConfirmDTO {

    /** 报名ID */
    @NotNull(message = "报名ID不能为空")
    private Long regId;

    /** 核定小时数 */
    @NotNull(message = "小时数不能为空")
    @Positive(message = "小时数必须大于0")
    private BigDecimal hours;

    /** 核定积分 */
    private Integer points;
}