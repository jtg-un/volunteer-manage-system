package com.vms.service;

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
}