package com.vms.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.vo.VolunteerRecordVO;

/**
 * 志愿者时长记录服务接口
 */
public interface VolunteerRecordService {

    /**
     * 获取志愿者的时长记录列表
     * @param userId 志愿者ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<VolunteerRecordVO> listRecords(Long userId, int page, int size);
}