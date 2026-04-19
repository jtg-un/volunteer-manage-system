package com.vms.common.dto;

import lombok.Data;

/**
 * AI推荐请求DTO
 */
@Data
public class AiRequestDTO {

    /** 用户问题（可选） */
    private String question;
}