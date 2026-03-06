package com.vms.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 活动查询DTO
 */
@Data
public class ActivityQueryDTO {

    /** 当前页 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;

    /** 项目属地编码 */
    private String regionCode;

    /** 服务类别 */
    private String categoryId;

    /** 状态 */
    private Integer status;

    /** 状态列表（多状态查询） */
    private List<Integer> statusList;

    /** 服务对象 */
    private String targetAudience;

    /** 关键词（标题模糊搜索） */
    private String keyword;
}