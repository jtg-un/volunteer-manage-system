package com.vms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.RegionDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.RegionVO;
import com.vms.repository.entity.SysRegion;
import com.vms.repository.mapper.SysRegionMapper;
import com.vms.system.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 行政区划服务实现
 */
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final SysRegionMapper regionMapper;

    @Override
    public List<RegionVO> getChildren(String parentCode) {
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();

        if (parentCode == null || parentCode.isEmpty()) {
            // 查询省级（parent_code 为空或 NULL）
            wrapper.and(w -> w.isNull(SysRegion::getParentCode)
                    .or()
                    .eq(SysRegion::getParentCode, ""));
            wrapper.orderByAsc(SysRegion::getRegionCode);
        } else {
            // 查询子级
            wrapper.eq(SysRegion::getParentCode, parentCode)
                   .orderByAsc(SysRegion::getRegionCode);
        }

        List<SysRegion> regions = regionMapper.selectList(wrapper);

        return regions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RegionVO> getRegionPage(int page, int size, Integer level, String keyword) {
        Page<SysRegion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<>();

        // 层级筛选
        if (level != null) {
            wrapper.eq(SysRegion::getLevel, level);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(SysRegion::getRegionCode, keyword)
                    .or().like(SysRegion::getRegionName, keyword)
            );
        }

        wrapper.orderByAsc(SysRegion::getRegionCode);

        Page<SysRegion> regionPage = regionMapper.selectPage(pageParam, wrapper);

        Page<RegionVO> voPage = new Page<>(regionPage.getCurrent(), regionPage.getSize(), regionPage.getTotal());
        voPage.setRecords(regionPage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public void addRegion(RegionDTO dto) {
        // 检查编码是否已存在
        SysRegion existing = regionMapper.selectById(dto.getRegionCode());
        if (existing != null) {
            throw new BusinessException(400, "区划编码已存在");
        }

        SysRegion region = new SysRegion();
        region.setRegionCode(dto.getRegionCode());
        region.setRegionName(dto.getRegionName());
        region.setParentCode(dto.getParentCode());
        region.setLevel(dto.getLevel());
        regionMapper.insert(region);
    }

    @Override
    public void updateRegion(RegionDTO dto) {
        SysRegion existing = regionMapper.selectById(dto.getRegionCode());
        if (existing == null) {
            throw new BusinessException(404, "区划不存在");
        }

        SysRegion region = new SysRegion();
        region.setRegionCode(dto.getRegionCode());
        region.setRegionName(dto.getRegionName());
        region.setParentCode(dto.getParentCode());
        region.setLevel(dto.getLevel());
        regionMapper.updateById(region);
    }

    @Override
    public void deleteRegion(String regionCode) {
        SysRegion region = regionMapper.selectById(regionCode);
        if (region == null) {
            throw new BusinessException(404, "区划不存在");
        }

        // 检查是否有子级
        LambdaQueryWrapper<SysRegion> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysRegion::getParentCode, regionCode);
        if (regionMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(400, "该区划下存在子级区划，无法删除");
        }

        regionMapper.deleteById(regionCode);
    }

    @Override
    public RegionVO getRegionDetail(String regionCode) {
        SysRegion region = regionMapper.selectById(regionCode);
        if (region == null) {
            throw new BusinessException(404, "区划不存在");
        }
        return convertToVO(region);
    }

    private RegionVO convertToVO(SysRegion region) {
        RegionVO vo = new RegionVO();
        vo.setRegionCode(region.getRegionCode());
        vo.setRegionName(region.getRegionName());
        vo.setParentCode(region.getParentCode());
        vo.setLevel(region.getLevel());

        // 检查是否有子级
        LambdaQueryWrapper<SysRegion> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysRegion::getParentCode, region.getRegionCode());
        long childCount = regionMapper.selectCount(childWrapper);
        vo.setHasChildren(childCount > 0);

        return vo;
    }
}