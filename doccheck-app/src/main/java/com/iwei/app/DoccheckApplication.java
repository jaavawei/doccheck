package com.iwei.app;

import com.iwei.common.config.ExternalConfigBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 文档查重与审查平台启动类
 *
 * @auther: zhaokangwei
 */
@SpringBootApplication
@ComponentScan("com.iwei")
@MapperScan("com.iwei.**.mapper")
public class DoccheckApplication extends SpringBootServletInitializer {

    private static ConfigurableApplicationContext applicationContext;

	/**
	 * main方法，注册极早期监听器
	 */
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(DoccheckApplication.class);
		// 极早期监听器，容器尚未启动，需要手动注册
		application.addListeners(new ExternalConfigBootstrap());
		applicationContext = application.run(args);
	}

	/**
	 * WAR 包部署时，给 tomcat 指定入口
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		application.listeners(new ExternalConfigBootstrap());
		return application.sources(DoccheckApplication.class);
	}
    
//    /**
//     * 应用关闭时的清理工作
//     */
//    private static void shutdown() {
//        if (applicationContext != null) {
//            try {
//                applicationContext.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        // 关闭Log4j2
//        LoggerContext context = (LoggerContext) LogManager.getContext(false);
//        if (context != null) {
//            try {
//                context.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}