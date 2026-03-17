package com.vms.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vms.repository.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价表Mapper
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {
}