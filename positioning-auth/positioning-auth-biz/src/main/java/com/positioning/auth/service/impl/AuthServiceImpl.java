package com.positioning.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.positioning.auth.dto.LoginRequest;
import com.positioning.auth.dto.LoginResponse;
import com.positioning.auth.dto.RegisterRequest;
import com.positioning.auth.dto.UserVO;
import com.positioning.auth.entity.SysLoginLog;
import com.positioning.auth.entity.SysUser;
import com.positioning.auth.mapper.SysLoginLogMapper;
import com.positioning.auth.mapper.SysUserMapper;
import com.positioning.auth.service.AuthService;
import com.positioning.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现: 登录/注册/登出/当前用户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        // 支持用户名或手机号登录
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getAccount())
                .or()
                .eq(SysUser::getPhone, request.getAccount())
                .last("LIMIT 1"));

        if (user == null) {
            saveLoginLog(null, request.getAccount(), ip, userAgent, false, "用户不存在");
            throw new BizException(400, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            saveLoginLog(user.getId(), user.getUsername(), ip, userAgent, false, "密码错误");
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            saveLoginLog(user.getId(), user.getUsername(), ip, userAgent, false, "账号被禁用");
            throw new BizException(403, "账号已被禁用");
        }

        // Sa-Token 登录（JWT 模式, token 无状态）
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 更新最后登录时间并记录日志
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(update);
        saveLoginLog(user.getId(), user.getUsername(), ip, userAgent, true, null);

        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());
        return new LoginResponse(token, toVO(user));
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO me() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 用户名与手机号唯一性校验
        Long usernameCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (usernameCount != null && usernameCount > 0) {
            throw new BizException(400, "用户名已存在");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            Long phoneCount = sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, request.getPhone()));
            if (phoneCount != null && phoneCount > 0) {
                throw new BizException(400, "手机号已被注册");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType("USER");
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user.getId();
    }

    private void saveLoginLog(Long userId, String username, String ip, String userAgent,
                              boolean success, String failReason) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setSuccess(success ? 1 : 0);
        log.setFailReason(failReason);
        log.setLoginAt(LocalDateTime.now());
        sysLoginLogMapper.insert(log);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setUserType(user.getUserType());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }
}
