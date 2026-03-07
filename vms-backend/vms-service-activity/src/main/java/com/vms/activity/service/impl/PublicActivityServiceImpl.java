package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.ActivityQueryDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.ActivityDetailVO;
import com.vms.common.vo.ActivityListVO;
import com.vms.activity.service.PublicActivityService;
import com.vms.activity.service.support.ActivitySupport;
import com.vms.repository.entity.Activity;
import com.vms.repository.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公开活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicActivityServiceImpl implements PublicActivityService {

    private final ActivityMapper activityMapper;
    private final ActivitySupport support;

    @Override
    public Page<ActivityListVO> list(ActivityQueryDTO query) {
        Page<Activity> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Activity> wrapper = buildQueryWrapper(query);
        wrapper.orderByDesc(Activity::getCreateTime);

        Page<Activity> result = activityMapper.selectPage(page, wrapper);
        return convertToVOPage(result);
    }

    @Override
    public ActivityDetailVO getDetail(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        return support.toDetailVO(activity);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Activity> buildQueryWrapper(ActivityQueryDTO query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        // 地区前缀匹配
        if (query.getRegionCode() != null && !query.getRegionCode().isEmpty()) {
            wrapper.likeRight(Activity::getRegionCode, query.getRegionCode().substring(0, 2));
        }
        // 服务类别
        if (query.getCategoryId() != null && !query.getCategoryId().isEmpty()) {
            wrapper.eq(Activity::getCategoryId, query.getCategoryId());
        }
        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(Activity::getStatus, query.getStatus());
        } else if (query.getStatusList() != null && !query.getStatusList().isEmpty()) {
            wrapper.in(Activity::getStatus, query.getStatusList());
        }
        // 目标人群
        if (query.getTargetAudience() != null && !query.getTargetAudience().isEmpty()) {
            wrapper.eq(Activity::getTargetAudience, query.getTargetAudience());
        }
        // 关键词搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Activity::getTitle, query.getKeyword());
        }

        return wrapper;
    }

    /**
     * 转换为 VO 分页结果
     */
    private Page<ActivityListVO> convertToVOPage(Page<Activity> result) {
        Page<ActivityListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ActivityListVO> voList = result.getRecords().stream()
                .map(support::toListVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}