package com.roger.common.security.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roger.api.service.RemoteUserService;
import com.roger.common.core.constant.ElAdminConstant;
import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.domain.dto.JwtUserDto;
import com.roger.common.core.domain.dto.UserDto;
import com.roger.common.core.exception.BadRequestException;
import com.roger.common.core.exception.ServiceException;
import com.roger.common.core.utils.RedisUtils;
import com.roger.common.core.utils.SecurityContextHolder;
import com.roger.common.core.utils.SpringContextHolder;
import com.roger.common.core.utils.StringUtils;
import com.roger.common.security.annotation.InnerAuth;
import com.roger.common.security.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Roger
 * @description:
 * @date: 2022/1/10 5:04 下午
 */
public class HeaderInterceptor implements AsyncHandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        String token = jwtUtil.getToken(request);
        if (StringUtils.isEmpty(token))
        {
            HandlerMethod hm;
            Method method;
            hm = (HandlerMethod) handler;
            // 获取方法
            method = hm.getMethod();
            // 该方法是否添加 允许未登录访问注解
            if (method.isAnnotationPresent(InnerAuth.class)) {
                // 不拦截
                return true;
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        jwtUtil.checkRenewal(token);
        SecurityContextHolder.setUserId(request.getHeader(SecurityConstants.DETAILS_USER_ID));
        String username = request.getHeader(SecurityConstants.DETAILS_USERNAME);
        SecurityContextHolder.setUserName(username);
        SecurityContextHolder.setLoginUser(getUserByUsername(username));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        SecurityContextHolder.remove();
    }

    public JwtUserDto getUserByUsername(String username) throws JsonProcessingException {
        RedisUtils redisUtils = SpringContextHolder.getBean(RedisUtils.class);
        RemoteUserService remoteUserService = SpringContextHolder.getBean(RemoteUserService.class);
        boolean searchDb = true;
        JwtUserDto jwtUserDto = null;
        if (redisUtils.hasKey(ElAdminConstant.USER_INFO_CACHE_PREFIX+username)) {
            jwtUserDto = (JwtUserDto) redisUtils.get(ElAdminConstant.USER_INFO_CACHE_PREFIX+username);
            searchDb = false;
        }
        if (searchDb) {
            UserDto user;

            ResponseEntity<UserDto> response = remoteUserService.findByName(username, SecurityConstants.INNER);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                user = response.getBody();
            } else {
                throw new ServiceException("服务内部按照用户名获取用户信息失败！");
            }

            if (user == null) {
                throw new UsernameNotFoundException("");
            } else {
                if (!user.getEnabled()) {
                    throw new BadRequestException("账号未激活！");
                }
                List<Long> dataScopes = Objects.requireNonNull(remoteUserService.getUserDataScope(user.getDeptId(), user.getId(), SecurityConstants.INNER).getBody());
                String authoritiesString = Objects.requireNonNull(remoteUserService.getUserAuthorities(user.getIsAdmin(), user.getId(), SecurityConstants.INNER).getBody());
                List<GrantedAuthority> authorities = new ArrayList<>();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(authoritiesString);
                Iterator<JsonNode> elements = jsonNode.elements();
                while (elements.hasNext()) {
                    JsonNode next = elements.next();
                    JsonNode authority = next.get("authority");
                    //将得到的值放入链表 最终返回该链表
                    authorities.add(new SimpleGrantedAuthority(authority.asText()));
                }
                jwtUserDto = new JwtUserDto(user, dataScopes, authorities);
                redisUtils.set(ElAdminConstant.USER_INFO_CACHE_PREFIX+username, jwtUserDto, 7, TimeUnit.DAYS);
            }
        }
        return jwtUserDto;
    }
}
