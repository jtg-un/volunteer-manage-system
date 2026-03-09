package com.vms.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.dto.RegistrationDTO;
import com.vms.common.exception.BusinessException;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import com.vms.activity.service.impl.VolunteerRegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 志愿者报名服务测试
 */
@ExtendWith(MockitoExtension.class)
class VolunteerRegistrationServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ActivityPositionMapper positionMapper;

    @Mock
    private CheckinLogMapper checkinLogMapper;

    @Mock
    private VolunteerRecordMapper recordMapper;

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private VolunteerRegistrationServiceImpl registrationService;

    private Activity testActivity;
    private ActivityPosition testPosition;
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

        testPosition = new ActivityPosition();
        testPosition.setPosId(1L);
        testPosition.setActivityId(1L);
        testPosition.setPosName("测试岗位");
        testPosition.setPlanCount(10);
        testPosition.setCurrentCount(5);

        testRegistration = new Registration();
        testRegistration.setRegId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setActivityId(1L);
        testPosition.setPosId(1L);
        testRegistration.setRegStatus(0);
    }

    @Test
    @DisplayName("报名活动 - 成功")
    void testRegister_Success() {
        // Arrange
        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(1L);
        dto.setPosId(1L);

        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(positionMapper.selectById(1L)).thenReturn(testPosition);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(registrationMapper.insert(any(Registration.class))).thenAnswer(invocation -> {
            Registration reg = invocation.getArgument(0);
            reg.setRegId(1L);
            return 1;
        });
        when(positionMapper.updateById(any(ActivityPosition.class))).thenReturn(1);

        // Act
        Long regId = registrationService.register(1L, dto);

        // Assert
        assertNotNull(regId);
        verify(registrationMapper).insert(any(Registration.class));
        verify(positionMapper).updateById(any(ActivityPosition.class));
    }

    @Test
    @DisplayName("报名活动 - 活动不存在")
    void testRegister_ActivityNotFound() {
        // Arrange
        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(999L);
        dto.setPosId(1L);

        when(activityMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> registrationService.register(1L, dto));
    }

    @Test
    @DisplayName("报名活动 - 活动未开始")
    void testRegister_ActivityNotStarted() {
        // Arrange
        testActivity.setStatus(0); // 待启动
        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(1L);
        dto.setPosId(1L);

        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.register(1L, dto));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("报名活动 - 岗位已满")
    void testRegister_PositionFull() {
        // Arrange
        testPosition.setCurrentCount(10); // 已满
        testPosition.setPlanCount(10);

        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(1L);
        dto.setPosId(1L);

        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(positionMapper.selectById(1L)).thenReturn(testPosition);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.register(1L, dto));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已报满"));
    }

    @Test
    @DisplayName("报名活动 - 重复报名")
    void testRegister_DuplicateRegistration() {
        // Arrange
        RegistrationDTO dto = new RegistrationDTO();
        dto.setActivityId(1L);
        dto.setPosId(1L);

        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(positionMapper.selectById(1L)).thenReturn(testPosition);
        when(registrationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.register(1L, dto));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("重复报名"));
    }

    @Test
    @DisplayName("取消报名 - 成功")
    void testCancel_Success() {
        // Arrange
        testRegistration.setRegStatus(0); // 待审核
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(registrationMapper.updateById(any(Registration.class))).thenReturn(1);

        // Act
        registrationService.cancel(1L, 1L);

        // Assert
        verify(registrationMapper).updateById(any(Registration.class));
    }

    @Test
    @DisplayName("取消报名 - 报名不存在")
    void testCancel_RegistrationNotFound() {
        // Arrange
        when(registrationMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> registrationService.cancel(1L, 999L));
    }

    @Test
    @DisplayName("取消报名 - 非本人报名")
    void testCancel_NotOwner() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> registrationService.cancel(999L, 1L)); // 不同的用户ID
        assertEquals(403, exception.getCode());
    }
}