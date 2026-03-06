package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 行政区划实体
 */
@Data
@TableName("sys_region")
public class SysRegion {

    /** 区划编码 */
    @TableId(type = IdType.INPUT)
    private String regionCode;

    /** 名称 */
    private String regionName;

    /** 父级编码 */
    private String parentCode;

    /** 层级: 1省 2市 3区 */
    private Integer level;
}