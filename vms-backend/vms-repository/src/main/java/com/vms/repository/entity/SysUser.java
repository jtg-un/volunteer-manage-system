package com.vms.repository.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户账号实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 加密密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色: 0志愿者 1组织负责人 2系统管理员 */
    private Integer role;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatarUrl;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}