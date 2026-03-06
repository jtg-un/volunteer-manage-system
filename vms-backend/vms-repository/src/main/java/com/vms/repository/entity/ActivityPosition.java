package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 活动岗位实体
 */
@Data
@TableName("activity_position")
public class ActivityPosition {

    @TableId(type = IdType.AUTO)
    private Long posId;

    /** 所属活动ID */
    private Long activityId;

    /** 岗位名称 */
    private String posName;

    /** 计划人数 */
    private Integer planCount;

    /** 当前已报名人数 */
    private Integer currentCount;
}