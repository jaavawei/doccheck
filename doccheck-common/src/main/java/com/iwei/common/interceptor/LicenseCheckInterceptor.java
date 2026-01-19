package com.iwei.common.interceptor;


import com.alibaba.fastjson2.JSON;
import com.iwei.common.entity.Result;
import com.iwei.common.enums.DefaultResponseCode;
import com.iwei.common.enums.ResultCodeEnum;
import com.iwei.common.tool.LicenseUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


/**
 * 使用拦截器拦截请求，验证证书的可用性
 *
 */
@Slf4j
@Component
public class LicenseCheckInterceptor implements HandlerInterceptor {


    @Value("${license.key:}")
    private String key;

    @Value("${license.content:}")
    private String content;

    @Resource
    private Environment env;



    /**
     * 进入controller层之前拦截请求
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("进入拦截器，验证证书可使用性");
        if(env.acceptsProfiles("dev")){
            return true;
        }
        boolean verifyResult = LicenseUtil.checkLicense(content, key);

        if(verifyResult){
            log.info("验证成功，证书可用");
            return true;
        }else{
            log.info("验证失败，证书无效");
            response.setCharacterEncoding("utf-8");
            Result<String> error = Result.result(ResultCodeEnum.LICENSE_AUTH_FAILD.getCode(),ResultCodeEnum.LICENSE_AUTH_FAILD.getDesc());

            response.getWriter().write(JSON.toJSONString(error));

            return false;
        }
    }


    /**
     * 处理请求完成后视图渲染之前的处理操作
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        log.info("处理请求完成后视图渲染之前的处理操作");
    }

    /**
     * 视图渲染之后的操作
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        log.info("视图渲染之后的操作");
    }

}
