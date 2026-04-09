package com.vms.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告 VO
 */
@Data
public class NoticeVO {

    private Integer noticeId;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 类型: 0通知 1公告 */
    private Integer type;

    /** 类型名称 */
    private String typeName;

    /** 发布人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;
}