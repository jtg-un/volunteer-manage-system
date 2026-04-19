package com.vms.activity.service;

import com.vms.common.dto.AiRequestDTO;
import com.vms.common.vo.AiRecommendVO;

/**
 * AI推荐服务接口
 */
public interface AiService {

    /**
     * 获取AI活动推荐
     * @param userId 用户ID
     * @param dto 请求参数
     * @return 推荐结果
     */
    AiRecommendVO recommend(Long userId, AiRequestDTO dto);
}