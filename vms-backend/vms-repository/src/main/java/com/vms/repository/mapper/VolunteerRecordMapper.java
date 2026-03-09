package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.VolunteerRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 志愿时长记录表Mapper
 */
@Mapper
public interface VolunteerRecordMapper extends BaseMapper<VolunteerRecord> {
}