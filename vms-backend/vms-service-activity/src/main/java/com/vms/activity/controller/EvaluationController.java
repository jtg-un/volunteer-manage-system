package com.vms.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.EvaluationDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.EvaluationVO;
import com.vms.activity.service.EvaluationService;
import com.vms.repository.entity.Organization;
import com.vms.repository.mapper.OrganizationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final OrganizationMapper organizationMapper;
    private final UserContext userContext;

    // ==================== 组织端接口 ====================

    /**
     * 组织评价志愿者
     */
    @PostMapping("/org/evaluation")
    public Result<Long> evaluate(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody EvaluationDTO dto) {
        Long userId = userContext.requireOrgAndGetUserId(authorization);
        Long orgId = getOrgIdByUserId(userId);
        Long evalId = evaluationService.evaluate(orgId, dto);
        return Result.success(evalId);
    }

    /**
     * 获取报名的评价信息
     */
    @GetMapping("/org/evaluation/{regId}")
    public Result<EvaluationVO> getByRegId(
            @PathVariable("regId") Long regId) {
        EvaluationVO vo = evaluationService.getByRegId(regId);
        return Result.success(vo);
    }

    /**
     * 检查报名是否已评价
     */
    @GetMapping("/org/evaluation/check/{regId}")
    public Result<Boolean> checkEvaluated(@PathVariable("regId") Long regId) {
        boolean evaluated = evaluationService.isEvaluated(regId);
        return Result.success(evaluated);
    }

    // ==================== 志愿者端接口 ====================

    /**
     * 志愿者获取自己的评价列表
     */
    @GetMapping("/volunteer/my/evaluations")
    public Result<Page<EvaluationVO>> listMyEvaluations(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = userContext.getUserId(authorization);
        Page<EvaluationVO> result = evaluationService.listMyEvaluations(userId, page, size);
        return Result.success(result);
    }

    /**
     * 根据用户ID获取组织ID
     */
    private Long getOrgIdByUserId(Long userId) {
        Organization org = organizationMapper.selectByUserId(userId);
        return org != null ? org.getOrgId() : null;
    }
}