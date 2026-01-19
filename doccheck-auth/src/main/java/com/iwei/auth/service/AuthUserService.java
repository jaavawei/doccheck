package com.iwei.auth.service;

import com.iwei.auth.entity.vo.AuthUserVo;

import java.util.List;

/**
 * 用户权限服务类
 *
 * @auther: zhaokangwei
 */
public interface AuthUserService {

    /**
     * 用户登录
     */
    String doLogin(AuthUserVo authUserVo);
}
