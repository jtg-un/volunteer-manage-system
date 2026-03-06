package com.vms.common.vo;

import lombok.Data;

/**
 * 行政区划VO
 */
@Data
public class RegionVO {

    /** 区划编码 */
    private String regionCode;

    /** 名称 */
    private String regionName;

    /** 父级编码 */
    private String parentCode;

    /** 层级: 1省 2市 3区 */
    private Integer level;

    /** 是否有子级 */
    private Boolean hasChildren;
}