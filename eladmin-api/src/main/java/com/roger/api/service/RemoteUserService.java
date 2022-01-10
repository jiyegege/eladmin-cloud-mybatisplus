package com.roger.api.service;

import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.constant.ServiceNameConstants;
import com.roger.common.core.domain.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 远程服务调用User Server
 *
 * @author roger
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.ELADMIN_SYSTEM_CORE)
public interface RemoteUserService {
    /**
     * 通过用户名查找用户
     * @param userName 用户名
     * @param source   接口请求来源
     * @return 用户信息
     */
    @GetMapping(value = "/api/users/findByName")
    ResponseEntity<UserDto> findByName(@RequestParam("userName") String userName, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过用户ID获取用户授权信息
     * @param isAdmin 是否是admin用户
     * @param userId 用户ID
     * @param source  接口请求来源
     * @return 用户授权信息
     */
    @GetMapping(value = "/api/users/{userId}/authorities")
    ResponseEntity<String> getUserAuthorities(@RequestParam("isAdmin") Boolean isAdmin, @PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 通过用户ID和部门ID获取用户的数据权限
     * @param deptId 部门ID
     * @param userId 用户ID
     * @param source 接口请求来源
     * @return 数据权限
     */
    @GetMapping("/api/users/{userId}/dataScopes")
    ResponseEntity<List<Long>> getUserDataScope(@RequestParam("deptId") Long deptId,@PathVariable("userId") Long userId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
