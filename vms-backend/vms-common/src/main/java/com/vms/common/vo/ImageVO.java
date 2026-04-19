package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片VO
 */
@Data
public class ImageVO {

    /** 文件ID */
    private Long fileId;

    /** 文件访问URL */
    private String fileUrl;

    /** 原始文件名 */
    private String originalName;

    /** 排序序号 */
    private Integer sortOrder;

    /** 是否封面 */
    private Integer isCover;

    /** 创建时间 */
    private LocalDateTime createTime;
}