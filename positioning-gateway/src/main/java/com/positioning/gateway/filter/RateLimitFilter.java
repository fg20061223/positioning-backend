package com.positioning.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 全局限流过滤器（按客户端 IP 令牌桶限流）
 * 采用 Caffeine 本地缓存, 无需额外部署 Redis;
 * 生产环境可替换为 Redis + Lua 实现分布式限流
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    /** 限流桶缓存: key=客户端IP, 一分钟过期 */
    private final Cache<String, TokenBucket> bucketCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /** 桶容量（突发上限） */
    @Value("${positioning.gateway.rate-limit.capacity:100}")
    private int capacity;

    /** 每分钟补充令牌数（稳定速率） */
    @Value("${positioning.gateway.rate-limit.refill-per-minute:100}")
    private int refillPerMinute;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() == null
                ? "unknown"
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        TokenBucket bucket = bucketCache.get(ip, k -> new TokenBucket(capacity, refillPerMinute));
        if (bucket == null || !bucket.tryAcquire()) {
            // 触发限流: 返回 429
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"code\":429,\"message\":\"请求过于频繁, 请稍后再试\",\"data\":null}";
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    /** 越早执行, 避免被路由过滤器跳过 */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 令牌桶
     */
    static class TokenBucket {

        private final int capacity;
        private final long refillNanos;
        private final AtomicLong tokens;
        private volatile long lastRefillNanos;

        TokenBucket(int capacity, int refillPerMinute) {
            this.capacity = capacity;
            this.refillNanos = TimeUnit.MINUTES.toNanos(1) / Math.max(1, refillPerMinute);
            this.tokens = new AtomicLong(capacity);
            this.lastRefillNanos = System.nanoTime();
        }

        /** 尝试获取一个令牌 */
        synchronized boolean tryAcquire() {
            refill();
            long current = tokens.get();
            if (current <= 0) {
                return false;
            }
            tokens.set(current - 1);
            return true;
        }

        /** 按时间补充令牌 */
        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            long added = elapsed / refillNanos;
            if (added > 0) {
                lastRefillNanos = now - elapsed % refillNanos;
                tokens.updateAndGet(v -> Math.min(capacity, v + added));
            }
        }
    }
}
