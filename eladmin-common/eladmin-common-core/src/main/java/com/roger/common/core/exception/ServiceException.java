package com.roger.common.core.exception;

/**
 * @Author: Roger
 * @description: 服务内部异常
 * @date: 2021/12/31 5:02 下午
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServiceException(String message)
    {
        super(message);
    }
}
