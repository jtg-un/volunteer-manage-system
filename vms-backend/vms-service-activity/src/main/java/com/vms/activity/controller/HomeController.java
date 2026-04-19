package com.vms.activity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.result.Result;
import com.vms.common.vo.HomeStatsVO;
import com.vms.common.vo.ImageVO;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.Registration;
import com.vms.repository.entity.SysFile;
import com.vms.repository.entity.SysUser;
import com.vms.repository.entity.VolunteerRecord;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.RegistrationMapper;
import com.vms.repository.mapper.SysFileMapper;
import com.vms.repository.mapper.SysUserMapper;
import com.vms.repository.mapper.VolunteerRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页公共控制器
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final SysUserMapper sysUserMapper;
    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final VolunteerRecordMapper volunteerRecordMapper;
    private final SysFileMapper sysFileMapper;

    /**
     * 获取首页统计数据
     */
    @GetMapping("/stats")
    public Result<HomeStatsVO> getStats() {
        HomeStatsVO vo = new HomeStatsVO();

        // 志愿者总数 (role=0)
        LambdaQueryWrapper<SysUser> volunteerWrapper = new LambdaQueryWrapper<>();
        volunteerWrapper.eq(SysUser::getRole, 0);
        Long volunteerCount = sysUserMapper.selectCount(volunteerWrapper);
        vo.setVolunteerCount(volunteerCount);

        // 组织总数 (role=1)
        LambdaQueryWrapper<SysUser> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(SysUser::getRole, 1);
        Long orgCount = sysUserMapper.selectCount(orgWrapper);
        vo.setOrgCount(orgCount);

        // 活动总数
        Long activityCount = activityMapper.selectCount(null);
        vo.setActivityCount(activityCount);

        // 运行中活动数 (status=1)
        LambdaQueryWrapper<Activity> runningWrapper = new LambdaQueryWrapper<>();
        runningWrapper.eq(Activity::getStatus, 1);
        Long runningCount = activityMapper.selectCount(runningWrapper);
        vo.setRunningActivityCount(runningCount);

        // 累计报名人次
        Long totalParticipations = registrationMapper.selectCount(null);
        vo.setTotalParticipations(totalParticipations);

        // 累计志愿时长 - BigDecimal 转换
        LambdaQueryWrapper<VolunteerRecord> hoursWrapper = new LambdaQueryWrapper<>();
        List<VolunteerRecord> records = volunteerRecordMapper.selectList(hoursWrapper);
        long totalHours = 0;
        for (VolunteerRecord record : records) {
            if (record.getHours() != null) {
                totalHours += record.getHours().longValue();
            }
        }
        vo.setTotalHours(totalHours);

        return Result.success(vo);
    }

    /**
     * 获取活动风采图片（最新活动的封面图）
     */
    @GetMapping("/gallery")
    public Result<List<ImageVO>> getGallery() {
        // 获取最新的活动封面图片
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, "activity_image")
               .eq(SysFile::getIsCover, 1)
               .orderByDesc(SysFile::getCreateTime)
               .last("LIMIT 8");
        List<SysFile> files = sysFileMapper.selectList(wrapper);

        List<ImageVO> list = files.stream().map(file -> {
            ImageVO vo = new ImageVO();
            vo.setFileId(file.getFileId());
            vo.setFileUrl(file.getFileUrl());
            vo.setOriginalName(file.getOriginalName());
            vo.setIsCover(file.getIsCover());
            vo.setCreateTime(file.getCreateTime());
            return vo;
        }).toList();

        return Result.success(list);
    }
}