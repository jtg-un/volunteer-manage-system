package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿时长记录表实体
 */
@Data
@TableName("volunteer_record")
public class VolunteerRecord {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    /** 关联报名ID */
    private Long regId;

    /** 核定小时数 */
    private BigDecimal hours;

    /** 核定积分 */
    private Integer points;

    /** 发放时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime auditTime;

    /** 审核人(负责人)ID */
    private Long auditorId;
}