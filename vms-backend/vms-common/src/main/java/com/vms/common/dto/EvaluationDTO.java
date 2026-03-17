package com.vms.common.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 评价提交DTO
 */
@Data
public class EvaluationDTO {

    /** 报名ID */
    @NotNull(message = "报名ID不能为空")
    private Long regId;

    /** 培训评分 (1-5分) */
    @NotNull(message = "培训评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低为1分")
    @DecimalMax(value = "5.0", message = "评分最高为5分")
    private BigDecimal scoreTraining;

    /** 协作评分 (1-5分) */
    @NotNull(message = "协作评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低为1分")
    @DecimalMax(value = "5.0", message = "评分最高为5分")
    private BigDecimal scoreCooperation;

    /** 执行力评分 (1-5分) */
    @NotNull(message = "执行力评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低为1分")
    @DecimalMax(value = "5.0", message = "评分最高为5分")
    private BigDecimal scoreExecution;

    /** 评价内容 */
    @Size(max = 500, message = "评价内容不能超过500字")
    private String comment;
}