package com.vms.system.controller;

import com.vms.common.result.Result;
import com.vms.common.vo.RegionVO;
import com.vms.system.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行政区划控制器
 */
@RestController
@RequestMapping("/api/system/region")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 根据父级编码获取子级区划
     * @param parentCode 父级编码，为空则查询省级
     */
    @GetMapping("/list")
    public Result<List<RegionVO>> getRegionList(
            @RequestParam(value = "parentCode", required = false) String parentCode) {
        List<RegionVO> list = regionService.getChildren(parentCode);
        return Result.success(list);
    }
}