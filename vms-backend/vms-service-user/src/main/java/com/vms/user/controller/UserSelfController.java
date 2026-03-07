package com.vms.user.controller;

import com.vms.common.context.UserContext;
import com.vms.common.dto.PasswordUpdateDTO;
import com.vms.common.dto.UserUpdateDTO;
import com.vms.common.result.Result;
import com.vms.common.vo.UserInfoVO;
import com.vms.user.service.UserSelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人控制器
 * 提供个人信息更新和密码修改接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserSelfController {

    private final UserSelfService userSelfService;
    private final UserContext userContext;

    /**
     * 更新个人信息
     */
    @PutMapping("/info")
    public Result<UserInfoVO> updateInfo(
            @Valid @RequestBody UserUpdateDTO dto,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.getUserId(authorization);
        UserInfoVO vo = userSelfService.updateInfo(userId, dto);
        return Result.success(vo);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateDTO dto,
            @RequestHeader("Authorization") String authorization) {

        Long userId = userContext.getUserId(authorization);
        userSelfService.updatePassword(userId, dto);
        return Result.successMsg("密码修改成功");
    }
}