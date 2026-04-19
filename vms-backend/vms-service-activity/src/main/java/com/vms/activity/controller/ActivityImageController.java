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
 * 活动图片控制器（简化版）
 */
@RestController
@RequestMapping("/api/activity-img")
@RequiredArgsConstructor
public class ActivityImageController {

    private final ImageService imageService;

    /**
     * 上传活动图片
     */
    @PostMapping("/upload")
    public Result<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("activityId") Long activityId,
            @RequestParam(value = "isCover", defaultValue = "0") Integer isCover) {
        ImageVO vo = imageService.uploadImage("activity_image", activityId, file, isCover);
        return Result.success(vo);
    }

    /**
     * 获取活动图片列表
     */
    @GetMapping("/{activityId}")
    public Result<List<ImageVO>> getImages(@PathVariable("activityId") Long activityId) {
        List<ImageVO> list = imageService.listImages("activity_image", activityId);
        return Result.success(list);
    }

    /**
     * 删除活动图片
     */
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteImage(@PathVariable("fileId") Long fileId) {
        imageService.deleteImage(fileId);
        return Result.successMsg("删除成功");
    }

    /**
     * 设置封面
     */
    @PutMapping("/cover/{fileId}")
    public Result<Void> setCover(@PathVariable("fileId") Long fileId) {
        imageService.setCover(fileId);
        return Result.successMsg("封面设置成功");
    }

    /**
     * 排序图片
     */
    @PutMapping("/sort")
    public Result<Void> sortImages(@RequestBody ImageSortDTO dto) {
        imageService.sortImages(dto);
        return Result.successMsg("排序更新成功");
    }
}