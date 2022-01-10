package com.roger.api.service;

import com.roger.common.core.constant.ServiceNameConstants;
import com.roger.common.core.domain.dto.SaveLogDTO;
import com.roger.common.core.constant.SecurityConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @Author: Roger
 * @description: log RPC
 * @date: 2022/1/6 9:49 上午
 */
@FeignClient(contextId = "remoteLogService", value = ServiceNameConstants.ELADMIN_LOG)
public interface RemoteLogService {
    /**
     * 保存日志
     * @param saveLogDTO 保存日志参数
     * @param source     请求来源
     * @return /
     */
    @PostMapping("/api/logs")
    ResponseEntity<Object> saveLog(@RequestBody SaveLogDTO saveLogDTO, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
