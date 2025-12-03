package com.sunlight.invest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 应用入口，启动 Spring Boot 并开启定时任务
 * <p>
 * 启动时会显示应用的启动耗时、访问地址和端口信息
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@SpringBootApplication
@EnableScheduling
public class FundApplication {
    
    /**
     * 应用程序主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 记录启动开始时间
        long startTime = System.currentTimeMillis();
        
        // 启动Spring Boot应用
        ConfigurableApplicationContext context = SpringApplication.run(FundApplication.class, args);
        
        // 计算启动耗时
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 获取环境配置
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        // 获取本机IP地址
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // 忽略异常，使用默认localhost
        }
        
        // 输出启动信息
        System.out.println("\n" +
                "----------------------------------------------------------\n" +
                "\t应用启动成功！\n" +
                "\t启动耗时: " + duration + " ms (" + String.format("%.2f", duration / 1000.0) + " 秒)\n" +
                "\t本地访问: \thttp://localhost:" + port + contextPath + "\n" +
                "\t外部访问: \thttp://" + hostAddress +":" + port + contextPath + "\n" +
                "\t通知测试: \thttp://localhost:" + port + "/notification-test.html\n" +
                "\t基金监控: \thttp://localhost:" + port + "/fund-monitor.html\n" +
                "\t指数监控: \thttp://localhost:" + port + "/index-data.html\n" +
                "----------------------------------------------------------"
        );
    }
}