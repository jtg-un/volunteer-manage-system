package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.EvaluationDTO;
import com.vms.common.vo.EvaluationVO;

/**
 * 评价服务接口
 */
public interface EvaluationService {

    /**
     * 组织评价志愿者
     * @param orgId 组织ID
     * @param dto 评价信息
     * @return 评价ID
     */
    Long evaluate(Long orgId, EvaluationDTO dto);

    /**
     * 获取报名的评价
     * @param regId 报名ID
     * @return 评价信息
     */
    EvaluationVO getByRegId(Long regId);

    /**
     * 志愿者获取自己的评价列表
     * @param userId 志愿者ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<EvaluationVO> listMyEvaluations(Long userId, int page, int size);

    /**
     * 检查报名是否已评价
     * @param regId 报名ID
     * @return 是否已评价
     */
    boolean isEvaluated(Long regId);
}