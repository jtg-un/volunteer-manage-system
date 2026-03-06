package com.vms.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 组织信息更新 DTO
 */
@Data
public class OrgUpdateDTO {

    /** 队伍全称 */
    @NotBlank(message = "队伍名称不能为空")
    private String orgName;

    /** 单位类型 */
    private String unitType;

    /** 属地编码 */
    private String regionCode;

    /** 成立日期 */
    private String foundDate;

    /** 联系人 */
    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 简介 */
    private String intro;
}