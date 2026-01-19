package com.iwei.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.iwei.common.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * mvc全局处理
 *
 * @auther: zhaokangwei
 */
@Configuration
public class GlobalConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 遍历转换器，找到默认的 Jackson 转换器并替换其 ObjectMapper
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter = (MappingJackson2HttpMessageConverter) converter;
                jacksonConverter.setObjectMapper(customObjectMapper());
                break;
            }
        }
    }

    /**
     * 自定义 ObjectMapper
     * 忽略null值、保留空字段
     */
    private ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 忽略 null 值字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 允许空对象序列化（不报错）
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }


//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new LoginInterceptor())
//                .addPathPatterns("/**")  // 拦截所有请求
//                .excludePathPatterns("/user/doLogin");  // 放行登录请求
//    }
}
