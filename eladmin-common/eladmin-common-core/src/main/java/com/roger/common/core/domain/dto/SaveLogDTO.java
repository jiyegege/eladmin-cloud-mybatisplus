package com.roger.common.core.domain.dto;

import com.roger.common.core.domain.Log;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

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
     * 日志对象
     */
    @NotNull
    @ApiModelProperty("日志对象")
    private Log log;

    /**
     * 方法名
     */
    @ApiModelProperty("方法名")
    private String methodName;

    /**
     * 方法参数
     */
    @ApiModelProperty("方法参数")
    private List<String> argValues;

    /**
     * 日志描述
     */
    @ApiModelProperty("日志描述")
    private String logDescription;
}
