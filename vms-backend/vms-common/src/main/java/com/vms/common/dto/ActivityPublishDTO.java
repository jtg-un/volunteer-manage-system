package com.vms.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布活动DTO
 */
@Data
public class ActivityPublishDTO {

    /** 项目标题 */
    @NotBlank(message = "项目标题不能为空")
    private String title;

    /** 服务类别 */
    @NotBlank(message = "服务类别不能为空")
    private String categoryId;

    /** 项目属地 */
    @NotBlank(message = "项目属地不能为空")
    private String regionCode;

    /** 服务对象 */
    private String targetAudience;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 项目描述 */
    private String description;

    /** 岗位列表 */
    @Valid
    @NotNull(message = "至少需要添加一个岗位")
    private List<PositionDTO> positions;

    @Data
    public static class PositionDTO {
        /** 岗位名称 */
        @NotBlank(message = "岗位名称不能为空")
        private String posName;

        /** 计划人数 */
        @NotNull(message = "计划人数不能为空")
        private Integer planCount;
    }
}