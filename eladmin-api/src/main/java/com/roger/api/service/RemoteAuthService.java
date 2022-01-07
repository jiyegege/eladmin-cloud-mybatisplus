package com.roger.api.service;

import com.roger.common.core.constant.ServiceNameConstants;
import com.roger.common.core.constant.SecurityConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Roger
 * @description: Auth PRC
 * @date: 2022/1/4 11:27 上午
 */
@FeignClient(contextId = "remoteAuthService", value = ServiceNameConstants.ELADMIN_AUTH)
public interface RemoteAuthService {
    /**
     * 清理特定用户缓存信息
     * @param userName 用户名
     * @param source 接口来源
     * @return /
     */
    @PostMapping(value = "/auth/user-cache:clean")
    ResponseEntity<Object> cleanUserCache(@RequestParam("user_name") String userName, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
