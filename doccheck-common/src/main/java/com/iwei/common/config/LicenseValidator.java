package com.iwei.common.config;

import com.iwei.common.Exception.ArgBusinessException;
import com.iwei.common.enums.ResultCodeEnum;
import com.iwei.common.tool.LicenseUtil;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


/**
 * @Author: 4611200202
 * @Date: 2024/10/12
 * @Description:
 */
@Component
@Slf4j
public class LicenseValidator implements ApplicationListener<ApplicationStartedEvent> {
    @Value("${license.key:}")
    private String key;

    @Value("${license.content:}")
    private String content;

    @Resource
    private Environment env;
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {

        try {
            if(!env.acceptsProfiles("dev")){
                key = "Sgepri.6186#2025";
                content = "BinJ4PbdoJA9wJT/UVss3HcobKDk/8aP09h5qzkf7mY8FOkSxSlynxLLD5oEq6X/ZTomGA8FQkXcHoklI2OJkA==";
                boolean b = LicenseUtil.checkLicense(content, key);
                if (b) {
                    log.info("授权验证通过！");
                } else {
                    log.error("授权验证失败");
                    System.exit(0);
                }
            }

        } catch (Exception e) {
            log.error("授权验证失败");
            System.exit(0);
            throw new ArgBusinessException(ResultCodeEnum.LICENSE_AUTH_FAILD, "授权未通过！");
        }


    }
}
