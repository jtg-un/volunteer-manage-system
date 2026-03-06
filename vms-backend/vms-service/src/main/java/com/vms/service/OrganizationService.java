package com.vms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.OrgAuditDTO;
import com.vms.common.dto.OrgUpdateDTO;
import com.vms.common.vo.OrgDetailVO;
import com.vms.common.vo.OrgListVO;

/**
 * 组织服务接口
 */
public interface OrganizationService {

    /**
     * 获取待审核组织列表（管理员）
     */
    Page<OrgListVO> getPendingList(int page, int size, Integer auditStatus);

    /**
     * 获取组织详情
     */
    OrgDetailVO getOrgDetail(Long orgId);

    /**
     * 审核组织（管理员）
     */
    void auditOrg(Long auditorId, OrgAuditDTO dto);

    /**
     * 获取当前组织信息
     */
    OrgDetailVO getMyOrg(Long userId);

    /**
     * 更新组织信息
     */
    void updateOrg(Long userId, OrgUpdateDTO dto);
}