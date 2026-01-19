package com.iwei.auth.controller;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.iwei.common.entity.Result;
import com.iwei.auth.entity.vo.AuthUserVo;
import com.iwei.auth.service.AuthUserService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户权限controller
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class AuthUserController {

    @Resource
    private AuthUserService authUserService;

    /**
     * 用户登录
     */
    @PostMapping("doLogin")
    public Result<String> doLogin(@RequestBody AuthUserVo authUserVo) {
        try {
            String token = authUserService.doLogin(authUserVo);
            if (token != null && !token.isEmpty()) {
                return Result.ok(token);
            } else {
                return Result.fail("账户名与密码不匹配");
            }
        } catch (Exception e) {
            log.error("UserController.doLogin.error:{}", e.getMessage(), e);
            return Result.fail("用户登录失败：未知错误");
        }
    }


}

