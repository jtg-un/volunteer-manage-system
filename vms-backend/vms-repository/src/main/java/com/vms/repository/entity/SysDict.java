package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 系统字典实体
 */
@Data
@TableName("sys_dict")
public class SysDict {

    @TableId(type = IdType.AUTO)
    private Integer dictId;

    /** 类型: service_category, unit_type, target_audience */
    private String dictType;

    /** 字典键 */
    private String dictKey;

    /** 字典值 */
    private String dictValue;
}