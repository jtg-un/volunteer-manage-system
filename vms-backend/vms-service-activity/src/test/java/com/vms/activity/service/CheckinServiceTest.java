package com.vms.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vms.common.exception.BusinessException;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import com.vms.activity.service.impl.CheckinServiceImpl;
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
 * 签到签退服务测试
 */
@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private CheckinLogMapper checkinLogMapper;

    @Mock
    private ActivityPositionMapper positionMapper;

    @InjectMocks
    private CheckinServiceImpl checkinService;

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
        testRegistration.setRegStatus(1); // 已通过
    }

    @Test
    @DisplayName("签到 - 成功")
    void testCheckIn_Success() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(checkinLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(checkinLogMapper.insert(any(CheckinLog.class))).thenReturn(1);

        // Act
        checkinService.checkIn(1L, 1L);

        // Assert
        verify(checkinLogMapper).insert(any(CheckinLog.class));
    }

    @Test
    @DisplayName("签到 - 报名不存在")
    void testCheckIn_RegistrationNotFound() {
        // Arrange
        when(registrationMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> checkinService.checkIn(1L, 999L));
    }

    @Test
    @DisplayName("签到 - 非本人报名")
    void testCheckIn_NotOwner() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkIn(999L, 1L));
        assertEquals(403, exception.getCode());
    }

    @Test
    @DisplayName("签到 - 报名未通过审核")
    void testCheckIn_NotApproved() {
        // Arrange
        testRegistration.setRegStatus(0); // 待审核
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkIn(1L, 1L));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("签到 - 活动未开始")
    void testCheckIn_ActivityNotStarted() {
        // Arrange
        testActivity.setStatus(0); // 待启动
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkIn(1L, 1L));
        assertEquals(400, exception.getCode());
    }

    @Test
    @DisplayName("签到 - 重复签到")
    void testCheckIn_AlreadyCheckedIn() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(checkinLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkIn(1L, 1L));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("重复签到"));
    }

    @Test
    @DisplayName("签退 - 成功")
    void testCheckOut_Success() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        // 已签到
        when(checkinLogMapper.selectCount(argThat(wrapper -> {
            LambdaQueryWrapper<CheckinLog> w = (LambdaQueryWrapper<CheckinLog>) wrapper;
            return true;
        }))).thenAnswer(invocation -> {
            LambdaQueryWrapper<CheckinLog> wrapper = invocation.getArgument(0);
            // 模拟签到记录存在
            return 1L;
        });
        // 使用更灵活的匹配
        when(checkinLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L, 0L);
        when(checkinLogMapper.insert(any(CheckinLog.class))).thenReturn(1);

        // Act
        checkinService.checkOut(1L, 1L);

        // Assert
        verify(checkinLogMapper).insert(any(CheckinLog.class));
    }

    @Test
    @DisplayName("签退 - 未签到先签退")
    void testCheckOut_NotCheckedIn() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        // 未签到
        when(checkinLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkOut(1L, 1L));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("先签到"));
    }

    @Test
    @DisplayName("签退 - 重复签退")
    void testCheckOut_AlreadyCheckedOut() {
        // Arrange
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        // 已签到
        when(checkinLogMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L, 1L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> checkinService.checkOut(1L, 1L));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("重复签退"));
    }
}