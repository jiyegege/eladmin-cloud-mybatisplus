package com.roger.common.security.annotation;

import java.lang.annotation.*;

/**
 * @Author: Roger
 * @description: 内部认证注解
 * @date: 2021/12/31 3:49 下午
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InnerAuth
{
    /**
     * 是否校验用户信息
     */
    boolean isUser() default false;
}
