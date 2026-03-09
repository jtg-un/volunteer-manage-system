package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.CheckinLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤流水表Mapper
 */
@Mapper
public interface CheckinLogMapper extends BaseMapper<CheckinLog> {
}