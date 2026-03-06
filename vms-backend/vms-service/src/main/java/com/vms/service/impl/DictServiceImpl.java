package com.vms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.vo.DictVO;
import com.vms.repository.entity.SysDict;
import com.vms.repository.mapper.SysDictMapper;
import com.vms.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务实现
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictMapper sysDictMapper;

    @Override
    public List<DictVO> getDictByType(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType);
        wrapper.orderByAsc(SysDict::getDictId);

        List<SysDict> dictList = sysDictMapper.selectList(wrapper);

        return dictList.stream().map(dict -> {
            DictVO vo = new DictVO();
            vo.setDictKey(dict.getDictKey());
            vo.setDictValue(dict.getDictValue());
            return vo;
        }).toList();
    }
}