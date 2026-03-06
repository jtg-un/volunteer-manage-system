package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动Mapper
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}