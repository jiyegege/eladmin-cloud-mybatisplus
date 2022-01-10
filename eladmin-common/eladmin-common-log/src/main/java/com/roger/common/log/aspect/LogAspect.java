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
package com.roger.common.log.aspect;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.roger.api.service.RemoteLogService;
import com.roger.common.core.constant.SecurityConstants;
import com.roger.common.core.domain.Log;
import com.roger.common.core.domain.dto.SaveLogDTO;
import com.roger.common.core.utils.RequestHolder;
import com.roger.common.core.utils.SecurityUtils;
import com.roger.common.core.utils.StringUtils;
import com.roger.common.core.utils.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
@Component
@Aspect
@Slf4j
public class LogAspect {

    private final RemoteLogService remoteLogService;

    ThreadLocal<Long> currentTime = new ThreadLocal<>();

    public LogAspect(RemoteLogService remoteLogService) {
        this.remoteLogService = remoteLogService;
    }

    /**
     * 配置切入点
     */
    @Pointcut("@annotation(com.roger.common.log.annotation.Log)")
    public void logPointcut() {
        // 该方法无方法体,主要为了让同类中其他方法使用此切入点
    }

    /**
     * 配置环绕通知,使用在方法logPointcut()上注册的切入点
     *
     * @param joinPoint join point for advice
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        currentTime.set(System.currentTimeMillis());
        result = joinPoint.proceed();
        Log log = new Log("INFO",System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        SaveLogDTO saveLogDTO = new SaveLogDTO();

        saveLogDTO.setBrowser(StringUtils.getBrowser(request));
        saveLogDTO.setIp(StringUtils.getIp(request));
        saveLogDTO.setLog(log);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.roger.common.log.annotation.Log aopLog = method.getAnnotation(com.roger.common.log.annotation.Log.class);
        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";
        //参数值
        List<String> argValues = new ArrayList<>();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            argValues.add(arg.toString());
        }
        String userName = getUsername();
        String loginPath = "login";
        if (loginPath.equals(signature.getName())) {
            try {
                userName = new JSONObject(args[0]).get("username").toString();
            } catch (Exception e) {
                LogAspect.log.error(e.getMessage(), e);
            }
        }
        saveLogDTO.setUsername(userName);
        saveLogDTO.setMethodName(methodName);
        saveLogDTO.setArgValues(argValues);
        saveLogDTO.setLogDescription(aopLog.value());
        remoteLogService.saveLog(saveLogDTO, SecurityConstants.INNER);
        return result;
    }

    /**
     * 配置异常通知
     *
     * @param joinPoint join point for advice
     * @param e exception
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Log log = new Log("ERROR",System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        log.setExceptionDetail(ThrowableUtil.getStackTrace(e).getBytes());
        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        SaveLogDTO saveLogDTO = new SaveLogDTO();
        saveLogDTO.setBrowser(StringUtils.getBrowser(request));
        saveLogDTO.setIp(StringUtils.getIp(request));
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.roger.common.log.annotation.Log aopLog = method.getAnnotation(com.roger.common.log.annotation.Log.class);
        // 方法路径
        String methodName = joinPoint.getTarget().getClass().getName() + "." + signature.getName() + "()";
        //参数值
        List<String> argValues = new ArrayList<>();
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            argValues.add(arg.toString());
        }
        String userName = getUsername();
        String loginPath = "login";
        if (loginPath.equals(signature.getName())) {
            try {
                userName = new JSONObject(argValues.get(0)).get("username").toString();
            } catch (Exception e1) {
                LogAspect.log.error(e1.getMessage(), e1);
            }
        }
        saveLogDTO.setUsername(userName);
        saveLogDTO.setMethodName(methodName);
        saveLogDTO.setArgValues(argValues);
        saveLogDTO.setLogDescription(aopLog.value());
        saveLogDTO.setLog(log);
        remoteLogService.saveLog(saveLogDTO, SecurityConstants.INNER);
    }

    public String getUsername() {
        try {
            return SecurityUtils.getCurrentUsername();
        }catch (Exception e){
            return "";
        }
    }
}
