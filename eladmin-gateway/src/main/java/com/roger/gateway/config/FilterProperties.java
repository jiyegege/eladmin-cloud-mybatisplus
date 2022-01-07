package com.roger.gateway.config;

/**
 * @Author: Roger
 * @description:
 * @date: 2021/10/13 10:35 下午
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "tianyu.bdp.filter")
public class FilterProperties {
    /**
     * 允许放行的URL
     */
    private List<String> allowPaths;
}
