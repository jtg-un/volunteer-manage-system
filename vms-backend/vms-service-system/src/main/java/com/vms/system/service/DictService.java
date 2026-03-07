package com.vms.system.service;

import com.vms.common.vo.DictVO;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService {

    /**
     * 根据类型获取字典列表
     */
    List<DictVO> getDictByType(String dictType);
}