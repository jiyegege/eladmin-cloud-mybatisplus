package com.roger.common.core.exception;

/**
 * @Author: ruoyi
 * @description: 内部认证异常
 * @date: 2021/12/31 3:58 下午
 */
public class InnerAuthException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public InnerAuthException(String message)
    {
        super(message);
    }
}
