package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体类
 */
@Data
@TableName("sys_file")
public class SysFile {

    @TableId(type = IdType.AUTO)
    private Long fileId;

    /** 业务类型: activity_image/org_gallery */
    @TableField("biz_type")
    private String bizType;

    /** 业务ID: activity_id/org_id */
    @TableField("biz_id")
    private Long bizId;

    /** 文件访问URL */
    @TableField("file_url")
    private String fileUrl;

    /** 原始文件名 */
    @TableField("original_name")
    private String originalName;

    /** 文件扩展名 */
    @TableField("file_type")
    private String fileType;

    /** 文件大小(bytes) */
    @TableField("file_size")
    private Long fileSize;

    /** 排序序号（越小越靠前） */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否封面: 0否 1是 */
    @TableField("is_cover")
    private Integer isCover;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}