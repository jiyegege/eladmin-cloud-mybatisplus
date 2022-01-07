package com.roger.common.core.domain.dto;

import com.roger.common.core.domain.Log;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: Roger
 * @description: 保存日志DTO
 * @date: 2022/1/6 10:07 上午
 */
@Data
public class SaveLogDTO {
    /**
     * 用户名
     */
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    /**
     * 浏览器信息
     */
    @ApiModelProperty("浏览器信息")
    private String browser;
    /**
     * IP
     */
    @ApiModelProperty("IP")
    private String ip;
    /**
     * 切面切入点信息
     */
    @NotNull
    @ApiModelProperty("切面切入点信息")
    private ProceedingJoinPoint joinPoint;
    /**
     * 日志对象
     */
    @NotNull
    @ApiModelProperty("日志对象")
    private Log log;
}
