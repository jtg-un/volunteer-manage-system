package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户账号 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}