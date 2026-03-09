package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 志愿队伍 Mapper
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 根据用户ID查询组织
     */
    @Select("SELECT * FROM organization WHERE user_id = #{userId}")
    Organization selectByUserId(Long userId);
}