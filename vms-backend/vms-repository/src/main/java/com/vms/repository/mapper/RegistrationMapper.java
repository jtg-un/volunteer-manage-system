package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.Registration;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报名表Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
}