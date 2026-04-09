package com.vms.common.vo;

import lombok.Data;

/**
 * 字典 VO
 */
@Data
public class DictVO {

    private Integer dictId;

    private String dictType;

    private String dictKey;

    private String dictValue;
}