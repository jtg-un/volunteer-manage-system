package com.vms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告新增/更新 DTO
 */
@Data
public class NoticeDTO {

    private Integer noticeId;

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 类型: 0通知 1公告 */
    private Integer type;
}