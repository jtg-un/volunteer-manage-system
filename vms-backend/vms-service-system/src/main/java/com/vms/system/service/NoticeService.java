package com.vms.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.NoticeDTO;
import com.vms.common.vo.NoticeVO;

import java.util.List;

/**
 * 公告服务接口
 */
public interface NoticeService {

    /**
     * 分页获取公告列表
     */
    Page<NoticeVO> getNoticePage(int page, int size, Integer type, String keyword);

    /**
     * 获取公告详情
     */
    NoticeVO getNoticeDetail(Integer noticeId);

    /**
     * 获取最新公告列表（公开）
     */
    List<NoticeVO> getLatestNotices(int limit);

    /**
     * 新增公告
     */
    void addNotice(NoticeDTO dto, String createBy);

    /**
     * 更新公告
     */
    void updateNotice(NoticeDTO dto);

    /**
     * 删除公告
     */
    void deleteNotice(Integer noticeId);
}