package com.vms.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.RegionDTO;
import com.vms.common.vo.RegionVO;

import java.util.List;

/**
 * 行政区划服务接口
 */
public interface RegionService {

    /**
     * 根据父级编码获取子级区划
     * @param parentCode 父级编码，为空则查询省级
     * @return 子级区划列表
     */
    List<RegionVO> getChildren(String parentCode);

    /**
     * 分页获取所有区划（管理员）
     */
    Page<RegionVO> getRegionPage(int page, int size, Integer level, String keyword);

    /**
     * 新增区划
     */
    void addRegion(RegionDTO dto);

    /**
     * 更新区划
     */
    void updateRegion(RegionDTO dto);

    /**
     * 删除区划
     */
    void deleteRegion(String regionCode);

    /**
     * 获取区划详情
     */
    RegionVO getRegionDetail(String regionCode);
}