package com.vms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典新增/更新 DTO
 */
@Data
public class DictDTO {

    private Integer dictId;

    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @NotBlank(message = "字典键不能为空")
    private String dictKey;

    @NotBlank(message = "字典值不能为空")
    private String dictValue;
}