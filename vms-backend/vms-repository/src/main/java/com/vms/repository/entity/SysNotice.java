package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Integer noticeId;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 类型: 0通知 1公告 */
    private Integer type;

    /** 发布人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;
}