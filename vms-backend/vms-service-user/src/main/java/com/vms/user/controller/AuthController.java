package com.vms.user.controller;

import com.vms.common.dto.LoginDTO;
import com.vms.common.dto.OrgRegisterDTO;
import com.vms.common.dto.VolunteerRegisterDTO;
import com.vms.common.vo.LoginVO;
import com.vms.common.vo.UserInfoVO;
import com.vms.common.result.Result;
import com.vms.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.secret:vms-secret-key-must-be-at-least-256-bits-long}")
    private String jwtSecret;

    /**
     * 志愿者注册
     */
    @PostMapping("/register/volunteer")
    public Result<Void> registerVolunteer(@Valid @RequestBody VolunteerRegisterDTO dto) {
        authService.registerVolunteer(dto);
        return Result.success("注册成功", null);
    }

    /**
     * 组织注册
     */
    @PostMapping("/register/org")
    public Result<Void> registerOrganization(@Valid @RequestBody OrgRegisterDTO dto) {
        authService.registerOrganization(dto);
        return Result.success("注册成功，请等待审核", null);
    }

    /**
     * 统一登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success(vo);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = claims.get("userId", Long.class);
        UserInfoVO vo = authService.getCurrentUser(userId);
        return Result.success(vo);
    }
}