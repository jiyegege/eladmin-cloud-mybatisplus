/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.roger.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.roger.api.service.RemoteUserService;
import com.roger.common.core.constant.ElAdminConstant;
import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.domain.dto.JwtUserDto;
import com.roger.common.core.domain.dto.UserDto;
import com.roger.common.core.exception.BadRequestException;
import com.roger.common.core.exception.ServiceException;
import com.roger.common.core.utils.RedisUtils;
import com.roger.common.security.utils.UserCacheClean;
import com.roger.security.config.LoginProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
@RequiredArgsConstructor
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    /**
     * 用户信息缓存
     *
     * @see {@link UserCacheClean}
     */
    //static Map<String, JwtUserDto> userDtoCache = new ConcurrentHashMap<>();
    private final RemoteUserService remoteUserService;
    private final LoginProperties loginProperties;
    private final RedisUtils redisUtils;

    public void setEnableCache(boolean enableCache) {
        this.loginProperties.setCacheEnable(enableCache);
    }

    @SneakyThrows
    @Override
    public JwtUserDto loadUserByUsername(String username) {
        boolean searchDb = true;
        JwtUserDto jwtUserDto = null;
        if (loginProperties.isCacheEnable() && redisUtils.hasKey(ElAdminConstant.USER_INFO_CACHE_PREFIX+username)) {
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
                //ResponseEntity<Object> responseEntity= Objects.requireNonNull(remoteUserService.getUserAuthorities(user.getIsAdmin(), user.getId(), SecurityConstants.INNER));
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
