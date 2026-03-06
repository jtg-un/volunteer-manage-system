package com.vms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.vo.RegionVO;
import com.vms.repository.entity.SysRegion;
import com.vms.repository.mapper.SysRegionMapper;
import com.vms.service.RegionService;
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