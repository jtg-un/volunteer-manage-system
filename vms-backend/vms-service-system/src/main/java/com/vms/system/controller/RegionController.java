package com.vms.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.RegionDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.RegionVO;
import com.vms.system.service.RegionService;
import jakarta.validation.Valid;
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

    // ==================== 公开接口 ====================

    /**
     * 根据父级编码获取子级区划（公开）
     * @param parentCode 父级编码，为空则查询省级
     */
    @GetMapping("/list")
    public Result<List<RegionVO>> getRegionList(
            @RequestParam(value = "parentCode", required = false) String parentCode) {
        List<RegionVO> list = regionService.getChildren(parentCode);
        return Result.success(list);
    }

    /**
     * 获取区划详情（公开）
     */
    @GetMapping("/detail/{regionCode}")
    public Result<RegionVO> getRegionDetail(@PathVariable("regionCode") String regionCode) {
        RegionVO vo = regionService.getRegionDetail(regionCode);
        return Result.success(vo);
    }

    // ==================== 管理员接口 ====================

    /**
     * 分页获取所有区划（管理员）
     */
    @GetMapping("/admin/page")
    public Result<Page<RegionVO>> getRegionPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<RegionVO> result = regionService.getRegionPage(page, size, level, keyword);
        return Result.success(result);
    }

    /**
     * 新增区划（管理员）
     */
    @PostMapping("/admin")
    public Result<Void> addRegion(@Valid @RequestBody RegionDTO dto) {
        regionService.addRegion(dto);
        return Result.successMsg("新增成功");
    }

    /**
     * 更新区划（管理员）
     */
    @PutMapping("/admin")
    public Result<Void> updateRegion(@Valid @RequestBody RegionDTO dto) {
        regionService.updateRegion(dto);
        return Result.successMsg("更新成功");
    }

    /**
     * 删除区划（管理员）
     */
    @DeleteMapping("/admin/{regionCode}")
    public Result<Void> deleteRegion(@PathVariable("regionCode") String regionCode) {
        regionService.deleteRegion(regionCode);
        return Result.successMsg("删除成功");
    }
}