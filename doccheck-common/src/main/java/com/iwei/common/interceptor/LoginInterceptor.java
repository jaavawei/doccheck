package com.iwei.common.interceptor;


import com.iwei.common.tool.LoginUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 放行登录请求
        if (requestURI.contains("/user/doLogin")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        // 检查用户是否已登录
        if (token == null || token.isEmpty() || !LoginUtil.isLogin(token)) {
            // 用户未登录，返回401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("未登录，请先登录");
            return false;
        }

        return true;
    }
}
