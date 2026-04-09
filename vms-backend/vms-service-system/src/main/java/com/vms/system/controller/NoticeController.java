package com.vms.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.NoticeDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.NoticeVO;
import com.vms.system.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ==================== 公开接口 ====================

    /**
     * 获取最新公告列表（公开）
     */
    @GetMapping("/latest")
    public Result<List<NoticeVO>> getLatestNotices(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<NoticeVO> list = noticeService.getLatestNotices(limit);
        return Result.success(list);
    }

    /**
     * 获取公告详情（公开）
     */
    @GetMapping("/detail/{noticeId}")
    public Result<NoticeVO> getNoticeDetail(@PathVariable("noticeId") Integer noticeId) {
        NoticeVO vo = noticeService.getNoticeDetail(noticeId);
        return Result.success(vo);
    }

    // ==================== 管理员接口 ====================

    /**
     * 分页获取公告列表（管理员）
     */
    @GetMapping("/admin/page")
    public Result<Page<NoticeVO>> getNoticePage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<NoticeVO> result = noticeService.getNoticePage(page, size, type, keyword);
        return Result.success(result);
    }

    /**
     * 新增公告（管理员）
     */
    @PostMapping("/admin")
    public Result<Void> addNotice(@Valid @RequestBody NoticeDTO dto) {
        noticeService.addNotice(dto, "管理员");
        return Result.successMsg("新增成功");
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/admin")
    public Result<Void> updateNotice(@Valid @RequestBody NoticeDTO dto) {
        noticeService.updateNotice(dto);
        return Result.successMsg("更新成功");
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/admin/{noticeId}")
    public Result<Void> deleteNotice(@PathVariable("noticeId") Integer noticeId) {
        noticeService.deleteNotice(noticeId);
        return Result.successMsg("删除成功");
    }
}