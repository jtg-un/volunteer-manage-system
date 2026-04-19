package com.vms.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 图片排序DTO
 */
@Data
public class ImageSortDTO {

    /** 业务类型: activity_image/org_gallery */
    private String bizType;

    /** 业务ID */
    private Long bizId;

    /** 排序项列表 */
    private List<SortItem> items;

    @Data
    public static class SortItem {
        /** 文件ID */
        private Long fileId;
        /** 排序序号 */
        private Integer sortOrder;
        /** 是否封面（可选） */
        private Integer isCover;
    }
}