package com.vms.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.dto.HoursConfirmDTO;
import com.vms.common.dto.RegistrationAuditDTO;
import com.vms.common.exception.BusinessException;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import com.vms.activity.service.impl.OrgRegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 组织报名管理服务测试
 */
@ExtendWith(MockitoExtension.class)
class OrgRegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ActivityPositionMapper positionMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private CheckinLogMapper checkinLogMapper;

    @Mock
    private VolunteerRecordMapper recordMapper;

    @InjectMocks
    private OrgRegistrationServiceImpl registrationService;

    private Activity testActivity;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        testActivity = new Activity();
        testActivity.setActivityId(1L);
        testActivity.setTitle("测试活动");
        testActivity.setStatus(1); // 运行中
        testActivity.setOrgId(1L);
        testActivity.setStartTime(LocalDateTime.now());
        testActivity.setEndTime(LocalDateTime.now().plusDays(7));

        testRegistration = new Registration();
        testRegistration.setRegId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setActivityId(1L);
        testRegistration.setPosId(1L);
        testRegistration.setRegStatus(0); // 待审核
    }

    @Test
    @DisplayName("审核报名 - 通过")
    void testAudit_Approve() {
        // Arrange
        RegistrationAuditDTO dto = new RegistrationAuditDTO();
        dto.setRegId(1L);
        dto.setStatus(1); // 通过

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        // Act
        registrationService.audit(1L, dto);

        // Assert
        verify(registrationMapper).updateById(any(Registration.class));
    }

    @Test
    @DisplayName("审核报名 - 拒绝")
    void testAudit_Reject() {
        // Arrange
        RegistrationAuditDTO dto = new RegistrationAuditDTO();
        dto.setRegId(1L);
        dto.setStatus(2); // 拒绝

        ActivityPosition position = new ActivityPosition();
        position.setPosId(1L);
        position.setCurrentCount(5);

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(positionMapper.selectById(1L)).thenReturn(position);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);
        when(positionMapper.updateById(any(ActivityPosition.class))).thenReturn(1);

        // Act
        registrationService.audit(1L, dto);

        // Assert
        verify(registrationMapper).updateById(any(Registration.class));
        verify(positionMapper).updateById(any(ActivityPosition.class));
    }

    @Test
    @DisplayName("审核报名 - 报名不存在")
    void testAudit_RegistrationNotFound() {
        // Arrange
        RegistrationAuditDTO dto = new RegistrationAuditDTO();
        dto.setRegId(999L);
        dto.setStatus(1);

        when(registrationMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> registrationService.audit(1L, dto));
    }

    @Test
    @DisplayName("审核报名 - 非本组织活动")
    void testAudit_NotOwner() {
        // Arrange
        RegistrationAuditDTO dto = new RegistrationAuditDTO();
        dto.setRegId(1L);
        dto.setStatus(1);

        testActivity.setOrgId(999L); // 不同组织
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.audit(1L, dto));
        assertEquals(403, exception.getCode());
    }

    @Test
    @DisplayName("审核报名 - 已处理的报名")
    void testAudit_AlreadyProcessed() {
        // Arrange
        testRegistration.setRegStatus(1); // 已通过
        RegistrationAuditDTO dto = new RegistrationAuditDTO();
        dto.setRegId(1L);
        dto.setStatus(1);

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.audit(1L, dto));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("发放时长 - 成功")
    void testConfirmHours_Success() {
        // Arrange
        testRegistration.setRegStatus(1); // 已通过
        HoursConfirmDTO dto = new HoursConfirmDTO();
        dto.setRegId(1L);
        dto.setHours(new BigDecimal("2.5"));

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(recordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(recordMapper.insert(any(VolunteerRecord.class))).thenReturn(1);

        // Act
        registrationService.confirmHours(1L, dto);

        // Assert
        verify(recordMapper).insert(any(VolunteerRecord.class));
    }

    @Test
    @DisplayName("发放时长 - 报名未通过")
    void testConfirmHours_NotApproved() {
        // Arrange
        testRegistration.setRegStatus(0); // 待审核
        HoursConfirmDTO dto = new HoursConfirmDTO();
        dto.setRegId(1L);
        dto.setHours(new BigDecimal("2.5"));

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.confirmHours(1L, dto));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("发放时长 - 已发放")
    void testConfirmHours_AlreadyIssued() {
        // Arrange
        testRegistration.setRegStatus(1);
        HoursConfirmDTO dto = new HoursConfirmDTO();
        dto.setRegId(1L);
        dto.setHours(new BigDecimal("2.5"));

        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(recordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.confirmHours(1L, dto));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已发放"));
    }
}