package com.roger.common.security.interceptor;

import com.roger.common.core.utils.SpringContextHolder;
import com.roger.common.core.utils.StringUtils;
import com.roger.common.security.utils.JwtUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
        jwtUtil.checkRenewal(token);
        return true;
    }
}
