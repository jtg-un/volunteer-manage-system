package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 志愿项目实体
 */
@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long activityId;

    /** 发起队伍 */
    private Long orgId;

    /** 项目编号 P开头 */
    private String projectCode;

    /** 项目标题 */
    private String title;

    /** 服务类别 */
    private String categoryId;

    /** 项目属地 */
    private String regionCode;

    /** 服务对象 */
    private String targetAudience;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态: 0待启动 1运行中 2已结项 3待审核 4拒绝 */
    private Integer status;

    /** 项目描述 */
    private String description;

    /** 拒绝原因 */
    private String rejectReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}