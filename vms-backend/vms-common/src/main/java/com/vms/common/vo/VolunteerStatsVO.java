package com.vms.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿者统计VO
 */
@Data
public class VolunteerStatsVO {

    /** 累计服务时长（小时） */
    private BigDecimal totalHours;

    /** 累计积分 */
    private Integer totalPoints;

    /** 参与活动次数 */
    private Integer activityCount;

    /** 本月服务时长 */
    private BigDecimal monthHours;

    /** 本年服务时长 */
    private BigDecimal yearHours;
}