package com.vms.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.DictDTO;
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

    /**
     * 分页获取所有字典（管理员）
     */
    Page<DictVO> getDictPage(int page, int size, String dictType, String keyword);

    /**
     * 新增字典
     */
    void addDict(DictDTO dto);

    /**
     * 更新字典
     */
    void updateDict(DictDTO dto);

    /**
     * 删除字典
     */
    void deleteDict(Integer dictId);

    /**
     * 获取所有字典类型列表
     */
    List<String> getDictTypes();
}