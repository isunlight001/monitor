package com.sunlight.invest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 应用入口，启动 Spring Boot 并开启定时任务
 * <p>
 * 启动时会显示应用的启动耗时、访问地址和端口信息
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@SpringBootApplication(scanBasePackages = {"com.sunlight.invest", "com.sunlight.ai"})
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
        
        // 输出启动开始信息
        System.out.println("==========================================================");
        System.out.println("  基金监控系统启动中...");
        System.out.println("  启动时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("  JVM版本: " + System.getProperty("java.version"));
        System.out.println("  操作系统: " + System.getProperty("os.name"));
        System.out.println("==========================================================");
        
        // 启动Spring Boot应用
        ConfigurableApplicationContext context = SpringApplication.run(FundApplication.class, args);
        
        // 计算启动耗时
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 获取环境配置
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        
        // 获取数据库配置信息
        String dbUrl = env.getProperty("spring.datasource.url", "未配置");
        String dbUsername = env.getProperty("spring.datasource.username", "未配置");
        
        // 获取邮件配置信息
        String mailHost = env.getProperty("spring.mail.host", "未配置");
        String mailUsername = env.getProperty("spring.mail.username", "未配置");
        
        // 获取本机IP地址
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // 忽略异常，使用默认localhost
        }
        
        // 输出详细的启动信息
        System.out.println("\n" +
                "----------------------------------------------------------\n" +
                "\t🎉 基金监控系统启动成功！\n" +
                "\t📅 启动时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "\t⏱️  启动耗时: " + duration + " ms (" + String.format("%.2f", duration / 1000.0) + " 秒)\n" +
                "\t🔌 监听端口: " + port + "\n" +
                "\t📂 上下文路径: " + (contextPath.isEmpty() ? "/" : contextPath) + "\n" +
                "\t💾 数据库URL: " + dbUrl + "\n" +
                "\t👤 数据库用户: " + dbUsername + "\n" +
                "\t📧 邮件服务器: " + mailHost + "\n" +
                "\t📩 邮件账户: " + mailUsername + "\n" +
                "\t🌐 本地访问: \thttp://localhost:" + port + contextPath + "\n" +
                "\t🌍 外部访问: \thttp://" + hostAddress + ":" + port + contextPath + "\n" +
                "\t🧪 通知测试: \thttp://localhost:" + port + "/notification-test.html\n" +
                "\t📊 基金监控: \thttp://localhost:" + port + "/fund-monitor.html\n" +
                "\t💹 指数监控: \thttp://localhost:" + port + "/index-data.html\n" +
                "\t📈 报告测试: \thttp://localhost:" + port + "/fund-report-test.html\n" +
                "\t👥 邮件接收人: \thttp://localhost:" + port + "/email-recipient-management.html\n" +
                "\t🤖 AI服务: \thttp://localhost:" + port + "/api/ai/chat?question=你好\n" +
                "----------------------------------------------------------"
        );
        
        // 输出内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        
        System.out.println(
                "🧠 JVM内存信息:\n" +
                "\t最大内存: " + maxMemory + " MB\n" +
                "\t已分配内存: " + totalMemory + " MB\n" +
                "\t已使用内存: " + usedMemory + " MB\n" +
                "\t可用内存: " + (maxMemory - usedMemory) + " MB\n" +
                "=========================================================="
        );
    }
}