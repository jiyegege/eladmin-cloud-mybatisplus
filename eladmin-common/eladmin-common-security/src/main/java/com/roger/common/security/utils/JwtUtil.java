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
package com.roger.common.security.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.roger.common.core.domain.dto.JwtUserDto;
import com.roger.common.core.utils.RedisUtils;
import com.roger.common.security.config.SecurityProperties;
import com.roger.common.core.constant.SecurityConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author /
 */
@Slf4j
@Component
public class JwtUtil implements InitializingBean {

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    public static final String AUTHORITIES_KEY = "auth";
    private JwtParser jwtParser;
    private JwtBuilder jwtBuilder;

    public JwtUtil(SecurityProperties properties, RedisUtils redisUtils) {
        this.properties = properties;
        this.redisUtils = redisUtils;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getBase64Secret());
        Key key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
        jwtBuilder = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS512);
    }

    /**
     * ??????Token ?????????????????????
     * Token ????????????????????????Redis ??????
     *
     * @param authentication /
     * @return /
     */
    public String createToken(Authentication authentication, JwtUserDto jwtUserDto) {
        Map<String, Object> claimsMap = new HashMap<>(2);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, jwtUserDto.getUser().getId());
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, jwtUserDto.getUsername());
        claimsMap.put(AUTHORITIES_KEY, authentication.getName());
        return jwtBuilder
                // ??????ID??????????????? Token ????????????
                .setId(IdUtil.simpleUUID())
                .setClaims(claimsMap)
                .setSubject(authentication.getName())
                .compact();
    }

    /**
     * ??????Token ??????????????????
     *
     * @param token /
     * @return /
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        User principal = new User(claims.getSubject(), "******", new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(principal, token, new ArrayList<>());
    }

    public Claims getClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * @param token ???????????????token
     */
    public void checkRenewal(String token) {
        // ??????????????????token,??????token???????????????
        long time = redisUtils.getExpire(properties.getOnlineKey() + token) * 1000;
        Date expireDate = DateUtil.offset(new Date(), DateField.MILLISECOND, (int) time);
        // ?????????????????????????????????????????????
        long differ = expireDate.getTime() - System.currentTimeMillis();
        // ?????????????????????????????????????????????
        if (differ <= properties.getDetect()) {
            long renew = time + properties.getRenew();
            redisUtils.expire(properties.getOnlineKey() + token, renew, TimeUnit.MILLISECONDS);
        }
    }

    public String getToken(HttpServletRequest request) {
        final String requestHeader = request.getHeader(properties.getHeader());
        if (requestHeader != null && requestHeader.startsWith(properties.getTokenStartWith())) {
            return requestHeader.substring(7);
        }
        return null;
    }

    /**
     * ????????????????????????ID
     *
     * @param token ??????
     * @return ??????ID
     */
    public String getUserId(String token)
    {
        Claims claims = getClaims(token);
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * ??????????????????????????????ID
     *
     * @param claims ????????????
     * @return ??????ID
     */
    public String getUserId(Claims claims)
    {
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * ???????????????????????????
     *
     * @param token ??????
     * @return ?????????
     */
    public String getUserName(String token)
    {
        Claims claims = getClaims(token);
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * ?????????????????????????????????
     *
     * @param claims ????????????
     * @return ?????????
     */
    public String getUserName(Claims claims)
    {
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * ??????????????????????????????
     *
     * @param claims ????????????
     * @param key ???
     * @return ???
     */
    public static String getValue(Claims claims, String key)
    {
        return Convert.toStr(claims.get(key), "");
    }
}
