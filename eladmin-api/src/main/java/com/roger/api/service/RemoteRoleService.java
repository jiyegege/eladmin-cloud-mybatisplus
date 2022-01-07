package com.roger.api.service;

import com.roger.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Roger
 * @description: 远程服务调用Role Server
 * @date: 2021/12/30 7:21 下午
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.ELADMIN_SYSTEM_CORE)
public interface RemoteRoleService {
    ResponseEntity<Object> getUserAuthorities(@RequestParam("isAdmin") Boolean isAdmin, @RequestParam("userId") Long userId);
}
