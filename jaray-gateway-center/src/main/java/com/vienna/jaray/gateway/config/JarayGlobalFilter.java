package com.vienna.jaray.gateway.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JarayGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();
        String path = exchange.getRequest().getPath().value();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        // 所有认证服务放行
        if (pathMatcher.match("/oauth/**", requestUrl)
                || pathMatcher.match("/jaray-auth-center/oauth/**", requestUrl)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest().mutate().build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
