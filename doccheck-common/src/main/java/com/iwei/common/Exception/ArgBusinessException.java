package com.iwei.common.Exception;

import com.iwei.common.enums.DefaultResponseCode;
import com.iwei.common.enums.ResultCodeEnum;
import lombok.AllArgsConstructor;

/**
 * 自定义异常
 *
 * @author: zs
 * @date: 2020/11/16 15:12
 * @description:
 */
@AllArgsConstructor
public class ArgBusinessException extends RuntimeException {

    private Integer code;

    private String message;

    private Object object;

    public ArgBusinessException() {
    }

    public ArgBusinessException(String message) {
        this.message = message;
    }

    public ArgBusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ArgBusinessException(String message, Object t) {
        this.message = message;
        this.object = t;
    }

    public ArgBusinessException(ResultCodeEnum serviceFail, String message) {
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
