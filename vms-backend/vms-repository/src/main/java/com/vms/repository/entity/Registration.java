package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名表实体
 */
@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long regId;

    private Long userId;

    private Long activityId;

    private Long posId;

    /** 状态: 0待审 1成功 2拒绝 3取消 */
    private Integer regStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}