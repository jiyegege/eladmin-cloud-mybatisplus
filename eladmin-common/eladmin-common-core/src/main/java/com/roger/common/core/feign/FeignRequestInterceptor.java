package com.roger.common.core.feign;

import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.utils.IpUtils;
import com.roger.common.core.utils.RequestHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: ruoyi
 * @description: feign 请求拦截器
 * @date: 2021/12/31 4:13 下午
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor
{
    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        HttpServletRequest httpServletRequest = RequestHolder.getHttpServletRequest();
        Map<String, String> headers = RequestHolder.getHeaders(httpServletRequest);
        // 传递用户信息请求头，防止丢失
        String userId = headers.get(SecurityConstants.DETAILS_USER_ID);
        if (StringUtils.isNotEmpty(userId))
        {
            requestTemplate.header(SecurityConstants.DETAILS_USER_ID, userId);
        }
        String userName = headers.get(SecurityConstants.DETAILS_USERNAME);
        if (StringUtils.isNotEmpty(userName))
        {
            requestTemplate.header(SecurityConstants.DETAILS_USERNAME, userName);
        }
        String authentication = headers.get(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(authentication))
        {
            requestTemplate.header(SecurityConstants.AUTHORIZATION_HEADER, authentication);
        }

        // 配置客户端IP
        requestTemplate.header("X-Forwarded-For", IpUtils.getIpAddr(RequestHolder.getHttpServletRequest()));
    }
}
