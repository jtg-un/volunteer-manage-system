package com.vms.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.DictDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.DictVO;
import com.vms.system.service.DictService;
import jakarta.validation.Valid;
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

    // ==================== 公开接口 ====================

    /**
     * 根据类型获取字典列表（公开）
     */
    @GetMapping("/{dictType}")
    public Result<List<DictVO>> getDictByType(@PathVariable("dictType") String dictType) {
        List<DictVO> list = dictService.getDictByType(dictType);
        return Result.success(list);
    }

    // ==================== 管理员接口 ====================

    /**
     * 分页获取所有字典（管理员）
     */
    @GetMapping("/admin/page")
    public Result<Page<DictVO>> getDictPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "dictType", required = false) String dictType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<DictVO> result = dictService.getDictPage(page, size, dictType, keyword);
        return Result.success(result);
    }

    /**
     * 新增字典（管理员）
     */
    @PostMapping("/admin")
    public Result<Void> addDict(@Valid @RequestBody DictDTO dto) {
        dictService.addDict(dto);
        return Result.successMsg("新增成功");
    }

    /**
     * 更新字典（管理员）
     */
    @PutMapping("/admin")
    public Result<Void> updateDict(@Valid @RequestBody DictDTO dto) {
        dictService.updateDict(dto);
        return Result.successMsg("更新成功");
    }

    /**
     * 删除字典（管理员）
     */
    @DeleteMapping("/admin/{dictId}")
    public Result<Void> deleteDict(@PathVariable("dictId") Integer dictId) {
        dictService.deleteDict(dictId);
        return Result.successMsg("删除成功");
    }

    /**
     * 获取所有字典类型（管理员）
     */
    @GetMapping("/admin/types")
    public Result<List<String>> getDictTypes() {
        List<String> types = dictService.getDictTypes();
        return Result.success(types);
    }
}