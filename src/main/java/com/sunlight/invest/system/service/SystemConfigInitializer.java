package com.sunlight.invest.system.service;

import com.sunlight.invest.system.entity.SystemConfig;
import com.sunlight.invest.system.mapper.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 系统配置初始化器
 * 在应用启动时初始化默认配置项
 *
 * @author System
 * @since 2024-12-04
 */
@Component
public class SystemConfigInitializer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(SystemConfigInitializer.class);
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统配置...");
        
        // 定义默认配置项
        List<SystemConfig> defaultConfigs = Arrays.asList(
            new SystemConfig("threshold_5_percent", "5.0", "5%阈值"),
            new SystemConfig("threshold_4_percent", "4.0", "4%阈值"),
            new SystemConfig("monitor_days", "7", "监控天数"),
            new SystemConfig("schedule_cron", "0 0 9 * * ?", "定时任务cron表达式")
        );
        
        // 检查并插入默认配置项
        for (SystemConfig config : defaultConfigs) {
            SystemConfig existingConfig = systemConfigMapper.selectByConfigKey(config.getConfigKey());
            if (existingConfig == null) {
                systemConfigMapper.insert(config);
                log.info("初始化配置项: {} = {}", config.getConfigKey(), config.getConfigValue());
            } else {
                log.info("配置项已存在，跳过: {}", config.getConfigKey());
            }
        }
        
        log.info("系统配置初始化完成");
    }
}