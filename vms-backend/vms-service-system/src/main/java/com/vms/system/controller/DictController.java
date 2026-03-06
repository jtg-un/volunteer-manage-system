package com.vms.system.controller;

import com.vms.common.result.Result;
import com.vms.common.vo.DictVO;
import com.vms.common.vo.RegionVO;
import com.vms.system.service.DictService;
import com.vms.system.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制器
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /**
     * 根据类型获取字典列表
     */
    @GetMapping("/{dictType}")
    public Result<List<DictVO>> getDictByType(@PathVariable("dictType") String dictType) {
        List<DictVO> list = dictService.getDictByType(dictType);
        return Result.success(list);
    }
}