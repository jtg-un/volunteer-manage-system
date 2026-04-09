package com.vms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 行政区划新增/更新 DTO
 */
@Data
public class RegionDTO {

    @NotBlank(message = "区划编码不能为空")
    private String regionCode;

    @NotBlank(message = "区划名称不能为空")
    private String regionName;

    private String parentCode;

    @NotNull(message = "层级不能为空")
    private Integer level;
}