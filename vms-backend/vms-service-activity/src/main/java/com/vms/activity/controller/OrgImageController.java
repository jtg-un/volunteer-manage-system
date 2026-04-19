package com.vms.activity.controller;

import com.vms.activity.service.ImageService;
import com.vms.common.dto.ImageSortDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.ImageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 组织风采控制器（简化版）
 */
@RestController
@RequestMapping("/api/org-img")
@RequiredArgsConstructor
public class OrgImageController {

    private final ImageService imageService;

    private static final String BIZ_TYPE = "org_gallery";

    /**
     * 上传组织风采
     */
    @PostMapping("/upload")
    public Result<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("orgId") Long orgId) {
        ImageVO vo = imageService.uploadImage(BIZ_TYPE, orgId, file, 0);
        return Result.success(vo);
    }

    /**
     * 获取组织风采列表
     */
    @GetMapping("/{orgId}")
    public Result<List<ImageVO>> getImages(@PathVariable("orgId") Long orgId) {
        List<ImageVO> list = imageService.listImages(BIZ_TYPE, orgId);
        return Result.success(list);
    }

    /**
     * 删除组织风采
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteImage(@PathVariable("fileId") Long fileId) {
        imageService.deleteImage(fileId);
        return Result.successMsg("删除成功");
    }

    /**
     * 排序组织风采
     */
    @PutMapping("/sort")
    public Result<Void> sortImages(@RequestBody ImageSortDTO dto) {
        imageService.sortImages(dto);
        return Result.successMsg("排序更新成功");
    }
}