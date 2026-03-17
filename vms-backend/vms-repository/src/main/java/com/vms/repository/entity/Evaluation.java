package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价表实体
 */
@Data
@TableName("evaluation")
public class Evaluation {

    @TableId(type = IdType.AUTO)
    private Long evalId;

    /** 关联报名ID */
    private Long regId;

    /** 培训评分 */
    private BigDecimal scoreTraining;

    /** 协作评分 */
    private BigDecimal scoreCooperation;

    /** 执行力评分 */
    private BigDecimal scoreExecution;

    /** 评价内容 */
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}