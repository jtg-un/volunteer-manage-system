package com.vms.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.activity.service.PublicActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开活动控制器
 * 提供无需认证的活动查询接口
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class PublicActivityController {

    private final PublicActivityService publicActivityService;

    /**
     * 活动列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<ActivityListVO>> list(ActivityQueryDTO query) {
        // 默认只查询审核通过的活动（状态 0,1,2）
        if (query.getStatus() == null) {
            query.setStatusList(java.util.Arrays.asList(0, 1, 2));
        }
        Page<ActivityListVO> result = publicActivityService.list(query);
        return Result.success(result);
    }

    /**
     * 活动详情
     */
    @GetMapping("/detail/{id}")
    public Result<ActivityDetailVO> detail(@PathVariable("id") Long id) {
        ActivityDetailVO vo = publicActivityService.getDetail(id);
        return Result.success(vo);
    }
}