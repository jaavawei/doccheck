package com.iwei.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

@WebListener
public class ContextFinalizer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 取消注册 JDBC 驱动以防止内存泄漏
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (Exception e) {
                // 记录但不中断其他驱动的注销过程
                e.printStackTrace();
            }
        }
        
        // 关闭Log4j2
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        if (context != null) {
            try {
                context.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 应用启动时不需要特殊处理
    }
}