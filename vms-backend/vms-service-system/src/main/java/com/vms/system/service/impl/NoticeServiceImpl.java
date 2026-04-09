package com.vms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.NoticeDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.NoticeVO;
import com.vms.repository.entity.SysNotice;
import com.vms.repository.mapper.SysNoticeMapper;
import com.vms.system.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public Page<NoticeVO> getNoticePage(int page, int size, Integer type, String keyword) {
        Page<SysNotice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();

        // 类型筛选
        if (type != null) {
            wrapper.eq(SysNotice::getType, type);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysNotice::getTitle, keyword);
        }

        wrapper.orderByDesc(SysNotice::getCreateTime);

        Page<SysNotice> noticePage = noticeMapper.selectPage(pageParam, wrapper);

        Page<NoticeVO> voPage = new Page<>(noticePage.getCurrent(), noticePage.getSize(), noticePage.getTotal());
        voPage.setRecords(noticePage.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public NoticeVO getNoticeDetail(Integer noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        return convertToVO(notice);
    }

    @Override
    public List<NoticeVO> getLatestNotices(int limit) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysNotice::getCreateTime)
               .last("LIMIT " + limit);
        List<SysNotice> notices = noticeMapper.selectList(wrapper);
        return notices.stream().map(this::convertToVO).toList();
    }

    @Override
    public void addNotice(NoticeDTO dto, String createBy) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType() != null ? dto.getType() : 0);
        notice.setCreateBy(createBy);
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
    }

    @Override
    public void updateNotice(NoticeDTO dto) {
        if (dto.getNoticeId() == null) {
            throw new BusinessException(400, "公告ID不能为空");
        }

        SysNotice existing = noticeMapper.selectById(dto.getNoticeId());
        if (existing == null) {
            throw new BusinessException(404, "公告不存在");
        }

        SysNotice notice = new SysNotice();
        notice.setNoticeId(dto.getNoticeId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        noticeMapper.updateById(notice);
    }

    @Override
    public void deleteNotice(Integer noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        noticeMapper.deleteById(noticeId);
    }

    private NoticeVO convertToVO(SysNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setType(notice.getType());
        vo.setTypeName(notice.getType() != null && notice.getType() == 1 ? "公告" : "通知");
        vo.setCreateBy(notice.getCreateBy());
        vo.setCreateTime(notice.getCreateTime());
        return vo;
    }
}