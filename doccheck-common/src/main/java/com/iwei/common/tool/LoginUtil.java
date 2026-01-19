package com.iwei.common.tool;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录工具类
 *
 * @author zhaokangwei
 */
@Component
public class LoginUtil {

    private static final ConcurrentHashMap<String, Long> loginMap = new ConcurrentHashMap<>();

    /*
     * 判断用户是否登录
     */
    public static boolean isLogin(String userName) {
        return loginMap.containsKey(userName);
    }

    /*
     * 登录
     */
    public static String login(String userName) {
        Long timeStamp= System.currentTimeMillis();
        loginMap.put(userName, timeStamp);
        return userName;
    }

    /*
     * 登出
     */
    public static void logout(String userName) {
        loginMap.remove(userName);
    }
}
