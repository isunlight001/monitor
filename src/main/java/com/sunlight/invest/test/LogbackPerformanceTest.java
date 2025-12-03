package com.sunlight.invest.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.AsyncAppender;

/**
 * Logback性能测试类，用于测试includeCallerData开启与关闭时的性能差异
 */
public class LogbackPerformanceTest {
    
    private static final Logger loggerWithCallerData = setupLogger("callerDataLogger", true);
    private static final Logger loggerWithoutCallerData = setupLogger("noCallerDataLogger", false);
    private static final int LOG_COUNT = 15000;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("开始测试Logback includeCallerData性能影响...");
        System.out.println("测试日志条数: " + LOG_COUNT);
        
        // 预热
        warmUp();
        
        // 测试不包含调用者数据的情况
        testPerformance(loggerWithoutCallerData, "不包含调用者数据");
        
        // 测试包含调用者数据的情况
        testPerformance(loggerWithCallerData, "包含调用者数据");
        
        System.out.println("测试完成!");
    }
    
    /**
     * 预热JVM
     */
    private static void warmUp() {
        System.out.println("预热阶段...");
        for (int i = 0; i < 10000; i++) {
            loggerWithoutCallerData.info("预热日志 {}", i);
            loggerWithCallerData.info("预热日志 {}", i);
        }
        try {
            Thread.sleep(1000); // 等待异步日志处理完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("预热完成\n");
    }
    
    /**
     * 测试日志性能
     *
     * @param logger 日志记录器
     * @param testName 测试名称
     */
    private static void testPerformance(Logger logger, String testName) {
        System.out.println("开始测试: " + testName);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < LOG_COUNT; i++) {
            logger.info("这是第{}条测试日志，测试名称: {}", i, testName);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("测试结果 - " + testName + ":");
        System.out.println("  耗时: " + duration + " ms");
        System.out.println("  平均每秒日志数: " + (LOG_COUNT * 1000 / duration) + " 条/秒");
        System.out.println();
        
        try {
            Thread.sleep(2000); // 等待异步日志处理完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 设置日志记录器
     *
     * @param name 记录器名称
     * @param includeCallerData 是否包含调用者数据
     * @return Logger对象
     */
    private static Logger setupLogger(String name, boolean includeCallerData) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // 创建控制台输出器
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName(name + "Console");
        
        // 设置日志格式
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        // 包含调用者信息的格式
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} %M:%L - %msg%n");
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        // 创建异步appender
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.setName(name + "Async");
        asyncAppender.setIncludeCallerData(includeCallerData);
        asyncAppender.setQueueSize(512);
        asyncAppender.addAppender(consoleAppender);
        asyncAppender.start();
        
        // 创建logger并设置appender
        ch.qos.logback.classic.Logger logger = context.getLogger(name);
        logger.setAdditive(false);
        logger.addAppender(asyncAppender);
        
        return logger;
    }
}