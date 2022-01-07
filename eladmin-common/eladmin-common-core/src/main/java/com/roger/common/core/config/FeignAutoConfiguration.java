package com.roger.common.core.config;

import feign.RequestInterceptor;
import com.roger.common.core.feign.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: ruoyi
 * @description: Feign配置
 * @date: 2021/12/31 4:12 下午
 */
@Configuration
public class FeignAutoConfiguration
{
    @Bean
    public RequestInterceptor requestInterceptor()
    {
        return new FeignRequestInterceptor();
    }
}
