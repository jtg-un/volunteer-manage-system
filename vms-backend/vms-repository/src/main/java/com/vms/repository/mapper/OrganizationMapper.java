package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.Organization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿队伍 Mapper
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
}