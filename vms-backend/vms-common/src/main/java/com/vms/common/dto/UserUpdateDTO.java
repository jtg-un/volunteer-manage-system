package com.vms.common.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户个人信息更新DTO
 */
@Data
public class UserUpdateDTO {

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 头像URL */
    private String avatarUrl;
}