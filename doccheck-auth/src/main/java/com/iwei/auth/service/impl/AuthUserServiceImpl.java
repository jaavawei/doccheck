package com.iwei.auth.service.impl;

import com.iwei.auth.entity.AuthUser;
import com.iwei.auth.entity.vo.AuthUserVo;
import com.iwei.auth.mapper.AuthUserMapper;
import com.iwei.auth.service.AuthUserService;
import com.iwei.auth.convert.AuthUserConvertor;
import com.iwei.common.tool.LoginUtil;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户权限服务实现类
 *
 * @auther: zhaokangwei
 */
@Service
public class AuthUserServiceImpl implements AuthUserService {

    @Resource
    private AuthUserMapper authUserMapper;

    /*
     * 用户登录
     */
    @Override
    public String doLogin(AuthUserVo authUserVo) {
        AuthUser authUser = AuthUserConvertor.INSTANCE.convertVoToAuthUser(authUserVo);
        List<AuthUser> authUserList = authUserMapper.queryByCondition(authUser);
        if(authUserList == null || authUserList.size() == 0) {
            return null;
        }
        String token = LoginUtil.login(authUserVo.getUsername());

        return token;
    }

}
