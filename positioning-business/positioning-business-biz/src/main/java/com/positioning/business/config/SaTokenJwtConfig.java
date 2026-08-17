package com.positioning.business.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token JWT 模式配置（token-style: jwt, 无状态）
 * <p>
 * 说明: 本项目为网关 + 认证/业务多服务架构, token 由认证服务签发、业务服务独立校验,
 * 必须使用 StpLogicJwtForStateless（jwt 无状态模式）:
 *   - jwt 模式: loginId 直接从 JWT 解析, 不依赖 Dao, 任意服务用同一 jwt-secret-key 即可校验;
 *   - jwt-simple 模式: token 虽是 JWT, 但 loginId 映射仍写入本地 Dao,
 *     业务服务(独立进程)查不到该映射, 会报 "token 无效"(401),
 *     仅适合单机或共享 Redis 的场景, 本项目不应使用。
 * Sa-Token 1.45.0 不会根据 sa-token.token-style 自动装配 StpLogic,
 * 需显式注册并 StpUtil.setStpLogic 应用到全局入口。
 */
@Slf4j
@Configuration
public class SaTokenJwtConfig {

    @Bean
    public StpLogic stpLogicJwt() {
        StpLogic stpLogic = new StpLogicJwtForStateless();
        // 关键: 将 JWT 无状态版 StpLogic 应用到全局静态入口
        StpUtil.setStpLogic(stpLogic);
        log.info("[Sa-Token] StpLogic 已切换为 JWT 无状态模式: {}, tokenStyle={}",
                stpLogic.getClass().getSimpleName(),
                cn.dev33.satoken.SaManager.getConfig().getTokenStyle());
        return stpLogic;
    }
}
