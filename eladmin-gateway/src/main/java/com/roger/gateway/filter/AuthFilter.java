package com.roger.gateway.filter;


import cn.hutool.core.net.URLEncodeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.domain.dto.OnlineUserDto;
import com.roger.common.core.utils.RedisUtils;
import com.roger.common.core.utils.StringUtils;
import com.roger.common.security.config.SecurityProperties;
import com.roger.common.security.utils.JwtUtil;
import com.roger.common.security.utils.UserCacheClean;
import com.roger.gateway.config.FilterProperties;
import com.roger.gateway.provider.ResponseProvider;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Author: Roger
 * @description:
 * @date: 2021/10/13 8:16 下午
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {
    private final FilterProperties filterProperties;
    private final RedisUtils redisUtils;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final UserCacheClean userCacheClean;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isSkip(path)) {
            return chain.filter(exchange);
        }
        ServerHttpResponse resp = exchange.getResponse();
        String headerAuthString = exchange.getRequest().getHeaders().getFirst(properties.getHeader());
        String paramAuthString = exchange.getRequest().getQueryParams().getFirst(properties.getHeader());
        if (StringUtils.isAllBlank(headerAuthString, paramAuthString)) {
            return unAuth(resp, "缺失令牌,鉴权失败");
        }
        String token = getToken(headerAuthString);
        Authentication claims = jwtUtil.getAuthentication(token);
        if (claims == null) {
            return unAuth(resp, "请求未授权或令牌已过期");
        }
        String userId = jwtUtil.getUserId(token);
        String userName = jwtUtil.getUserName(token);
        OnlineUserDto onlineUserDto = null;
        boolean cleanUserCache = false;
        try {
            onlineUserDto = (OnlineUserDto)redisUtils.get(properties.getOnlineKey() + token);
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            cleanUserCache = true;
        } finally {
            if (cleanUserCache || Objects.isNull(onlineUserDto)) {
                userCacheClean.cleanUserCache(userName);
            }
        }
        if (onlineUserDto != null && org.springframework.util.StringUtils.hasText(token)) {
            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Token 续期
            jwtUtil.checkRenewal(token);
        }
        ServerHttpRequest.Builder mutate = exchange.getRequest().mutate();
        // 设置用户信息到请求
        addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userId);
        addHeader(mutate, SecurityConstants.DETAILS_USERNAME, userName);
        // 内部请求来源参数清除
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange);
    }

    private boolean isSkip(String path) {
        return filterProperties.getAllowPaths().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private Mono<Void> unAuth(ServerHttpResponse resp, String msg) {
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String result = "";
        try {
            result = objectMapper.writeValueAsString(ResponseProvider.unAuth(msg));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        DataBuffer buffer = resp.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }

    /**
     * 获取token
     * @param authString 授权字符串
     * @return token
     */
    public String getToken(String authString) {
        if (StringUtils.isNotBlank(authString) && authString.startsWith(properties.getTokenStartWith())) {
            return authString.substring(7);
        }
        return null;
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name)
    {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value)
    {
        if (value == null)
        {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = URLEncodeUtil.encode(valueStr, StandardCharsets.UTF_8);
        mutate.header(name, valueEncode);
    }


    @Override
    public int getOrder() {
        return -100;
    }
}
