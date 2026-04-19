package com.vms.common.vo;

import lombok.Data;

import java.util.List;

/**
 * AI推荐结果VO
 */
@Data
public class AiRecommendVO {

    /** AI回复文本 */
    private String answer;

    /** 推荐的活动列表 */
    private List<ActivitySimpleVO> activities;

    /**
     * 活动简要信息VO（用于推荐展示）
     */
    @Data
    public static class ActivitySimpleVO {
        /** 活动ID */
        private Long activityId;

        /** 活动标题 */
        private String title;

        /** 发起组织名称 */
        private String orgName;

        /** 服务类别名称 */
        private String categoryName;

        /** 地区名称 */
        private String regionName;

        /** 开始时间 */
        private String startTime;

        /** 结束时间 */
        private String endTime;

        /** 封面图片URL */
        private String coverImageUrl;

        /** 报名人数 */
        private Integer currentCount;

        /** 计划人数 */
        private Integer planCount;
    }
}