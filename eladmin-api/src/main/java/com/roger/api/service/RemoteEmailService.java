package com.roger.api.service;

import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.constant.ServiceNameConstants;
import com.roger.common.core.domain.vo.EmailVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @Author: Roger
 * @description: Email RPC
 * @date: 2022/1/6 6:53 下午
 */
@FeignClient(contextId = "remoteEmailService", value = ServiceNameConstants.ELADMIN_SYSTEM_CORE)
public interface RemoteEmailService {
    /**
     * 发送邮件
     * @param emailVo 邮件信息
     * @param source 请求来源
     * @return /
     */
    ResponseEntity<Object> sendEmail(@Validated @RequestBody EmailVo emailVo, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
