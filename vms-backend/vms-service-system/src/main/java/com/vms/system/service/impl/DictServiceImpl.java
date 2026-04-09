package com.vms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.DictDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.DictVO;
import com.vms.repository.entity.SysDict;
import com.vms.repository.mapper.SysDictMapper;
import com.vms.system.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        return dictList.stream().map(this::convertToVO).toList();
    }

    @Override
    public Page<DictVO> getDictPage(int page, int size, String dictType, String keyword) {
        Page<SysDict> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();

        // 类型筛选
        if (dictType != null && !dictType.isBlank()) {
            wrapper.eq(SysDict::getDictType, dictType);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(SysDict::getDictKey, keyword)
                    .or().like(SysDict::getDictValue, keyword)
            );
        }

        wrapper.orderByAsc(SysDict::getDictType, SysDict::getDictId);

        Page<SysDict> dictPage = sysDictMapper.selectPage(pageParam, wrapper);

        Page<DictVO> voPage = new Page<>(dictPage.getCurrent(), dictPage.getSize(), dictPage.getTotal());
        voPage.setRecords(dictPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public void addDict(DictDTO dto) {
        // 检查是否已存在相同类型和键
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dto.getDictType())
               .eq(SysDict::getDictKey, dto.getDictKey());
        if (sysDictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该字典类型下已存在相同的键");
        }

        SysDict dict = new SysDict();
        dict.setDictType(dto.getDictType());
        dict.setDictKey(dto.getDictKey());
        dict.setDictValue(dto.getDictValue());
        sysDictMapper.insert(dict);
    }

    @Override
    public void updateDict(DictDTO dto) {
        if (dto.getDictId() == null) {
            throw new BusinessException(400, "字典ID不能为空");
        }

        SysDict existing = sysDictMapper.selectById(dto.getDictId());
        if (existing == null) {
            throw new BusinessException(404, "字典不存在");
        }

        // 检查键是否重复（排除自身）
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dto.getDictType())
               .eq(SysDict::getDictKey, dto.getDictKey())
               .ne(SysDict::getDictId, dto.getDictId());
        if (sysDictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该字典类型下已存在相同的键");
        }

        SysDict dict = new SysDict();
        dict.setDictId(dto.getDictId());
        dict.setDictType(dto.getDictType());
        dict.setDictKey(dto.getDictKey());
        dict.setDictValue(dto.getDictValue());
        sysDictMapper.updateById(dict);
    }

    @Override
    public void deleteDict(Integer dictId) {
        SysDict dict = sysDictMapper.selectById(dictId);
        if (dict == null) {
            throw new BusinessException(404, "字典不存在");
        }
        sysDictMapper.deleteById(dictId);
    }

    @Override
    public List<String> getDictTypes() {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDict::getDictType).groupBy(SysDict::getDictType);
        List<SysDict> list = sysDictMapper.selectList(wrapper);
        return list.stream().map(SysDict::getDictType).toList();
    }

    private DictVO convertToVO(SysDict dict) {
        DictVO vo = new DictVO();
        vo.setDictId(dict.getDictId());
        vo.setDictType(dict.getDictType());
        vo.setDictKey(dict.getDictKey());
        vo.setDictValue(dict.getDictValue());
        return vo;
    }
}