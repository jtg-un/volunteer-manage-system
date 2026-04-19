package com.vms.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.activity.service.ImageService;
import com.vms.common.context.UserContext;
import com.vms.common.result.Result;
import com.vms.common.vo.ImageVO;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.OrganizationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织风采控制器（带认证的接口）
 */
@RestController
@RequestMapping("/api/org/images")
@RequiredArgsConstructor
public class OrgGalleryController {

    private final ImageService imageService;
    private final OrganizationMapper organizationMapper;
    private final UserContext userContext;

    private static final String BIZ_TYPE = "org_gallery";

    /**
     * 获取当前组织的风采图片
     */
    @GetMapping("/my")
    public Result<List<ImageVO>> getMyGallery(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // 从token获取用户ID
        Long userId = userContext.getUserId(authorization);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 根据用户ID查询组织ID
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getUserId, userId);
        Organization org = organizationMapper.selectOne(wrapper);
        if (org == null) {
            return Result.error(404, "组织不存在");
        }

        // 获取组织风采图片
        List<ImageVO> list = imageService.listImages(BIZ_TYPE, org.getOrgId());
        return Result.success(list);
    }
}