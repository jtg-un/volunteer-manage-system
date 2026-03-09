package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考勤流水表实体
 */
@Data
@TableName("checkin_log")
public class CheckinLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    /** 关联报名ID */
    private Long regId;

    /** 签到/签退时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime checkTime;

    /** 类型: 0签到 1签退 */
    private Integer checkType;
}