package com.vms.activity.controller;

import com.vms.common.context.UserContext;
import com.vms.common.dto.AiRequestDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.AiRecommendVO;
import com.vms.activity.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI推荐控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final UserContext userContext;

    /**
     * 获取AI活动推荐
     */
    @PostMapping("/recommend")
    public Result<AiRecommendVO> recommend(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) AiRequestDTO dto) {
        Long userId = userContext.getUserId(authorization);
        if (dto == null) {
            dto = new AiRequestDTO();
        }
        AiRecommendVO result = aiService.recommend(userId, dto);
        return Result.success(result);
    }
}