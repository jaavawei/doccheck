package com.iwei.common.enums;

/**
 * @Author: 4611200202
 * @Date: 2024/9/2
 * @Description:
 */
public enum DefaultResponseCode {

    OK(0, "OK"),
    UNKNOWN_ERROR(1, "未知异常"),
    SERVICE_FAIL(2, "服务异常"),
    PAGE_ERROR(3, "页码异常"),
    LACK_PARAMS(4, "缺少参数"),
    PARAMS_ERROR(5, "参数异常"),
    RERQUEST_ERROR(10, "调用接口异常"),
    SERVICE_ERROR(14, "调用接口异常"),
    //提示词库异常
    NAME_PARAMS_ERROR(6, "模板名称参数异常"),
    TAG_PARAMS_ERROR(7, "模板标签参数异常"),
    CONTENT_PARAMS_ERROR(8, "prompt参数异常"),
    NAME_REPEAT_ERROR(9, "名称重复"),
    REQUEST_ERROR(10,"调用接口异常"),
    CAPTCHA_EMPTY(11, "验证码不能为空"),
    CAPTCHA_ERROR(12, "验证码错误"),

    LICENSE_AUTH_FAILD(13,"授权未通过"),
    ACCOUNT_PASSWORD_EMPTY(21, "账号和密码均不能为空"),
    ACCOUNT_NOT_EXIST(22, "账号不存在"),
    ACCOUNT_PASSWORD_ERROR(23, "账号密码错误"),
    ACCOUNT_LOCKED(24, "账号锁定"),
    ACCOUNT_EXPIRED(25, "账号过期"),
    ACCOUNT_DISABLED(26, "账号禁用"),
    ACCOUNT_PASSWORD_RETRY_ERROR(27, "密码输入错误 {total} 次账号将被锁定, 您还能再试 {remain} 次"),
    ACCOUNT_PASSWORD_WEAK(28, "密码强度低"),
    ACCESS_DENIED(403, "越权访问"),
    NOT_FOUND(404, "未找到指定资源");


    public final int code;
    public final String msg;

    private DefaultResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
