package com.vms.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.context.UserContext;
import com.vms.common.dto.RegistrationDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.MyRegistrationVO;
import com.vms.common.vo.VolunteerRecordVO;
import com.vms.common.vo.VolunteerStatsVO;
import com.vms.activity.service.CheckinService;
import com.vms.activity.service.VolunteerRecordService;
import com.vms.activity.service.VolunteerRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 志愿者端控制器
 */
@RestController
@RequestMapping("/api/volunteer")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerRegistrationService registrationService;
    private final CheckinService checkinService;
    private final VolunteerRecordService recordService;
    private final UserContext userContext;

    // ==================== 报名相关 ====================

    /**
     * 报名活动
     */
    @PostMapping("/register")
    public Result<Long> register(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody RegistrationDTO dto) {
        Long userId = userContext.getUserId(authorization);
        Long regId = registrationService.register(userId, dto);
        return Result.success(regId);
    }

    /**
     * 取消报名
     */
    @DeleteMapping("/register/{regId}")
    public Result<Void> cancel(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("regId") Long regId) {
        Long userId = userContext.getUserId(authorization);
        registrationService.cancel(userId, regId);
        return Result.successMsg("取消报名成功");
    }

    /**
     * 获取我的报名列表
     */
    @GetMapping("/my/registrations")
    public Result<Page<MyRegistrationVO>> listMyRegistrations(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = userContext.getUserId(authorization);
        Page<MyRegistrationVO> result = registrationService.listMyRegistrations(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取志愿者统计数据
     */
    @GetMapping("/my/stats")
    public Result<VolunteerStatsVO> getStats(
            @RequestHeader("Authorization") String authorization) {
        Long userId = userContext.getUserId(authorization);
        VolunteerStatsVO stats = registrationService.getStats(userId);
        return Result.success(stats);
    }

    // ==================== 签到相关 ====================

    /**
     * 签到
     */
    @PostMapping("/checkin/{regId}")
    public Result<Void> checkIn(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("regId") Long regId) {
        Long userId = userContext.getUserId(authorization);
        checkinService.checkIn(userId, regId);
        return Result.successMsg("签到成功");
    }

    /**
     * 签退
     */
    @PostMapping("/checkout/{regId}")
    public Result<Void> checkOut(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("regId") Long regId) {
        Long userId = userContext.getUserId(authorization);
        checkinService.checkOut(userId, regId);
        return Result.successMsg("签退成功");
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/checkin/status/{regId}")
    public Result<MyRegistrationVO> getCheckinStatus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("regId") Long regId) {
        Long userId = userContext.getUserId(authorization);
        MyRegistrationVO status = checkinService.getCheckinStatus(userId, regId);
        return Result.success(status);
    }

    // ==================== 时长记录 ====================

    /**
     * 获取我的时长记录
     */
    @GetMapping("/my/records")
    public Result<Page<VolunteerRecordVO>> listMyRecords(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = userContext.getUserId(authorization);
        Page<VolunteerRecordVO> result = recordService.listRecords(userId, page, size);
        return Result.success(result);
    }
}