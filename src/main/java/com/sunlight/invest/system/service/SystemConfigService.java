package com.sunlight.invest.system.service;

import com.sunlight.invest.system.entity.SystemConfig;
import com.sunlight.invest.system.mapper.SystemConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务类
 *
 * @author System
 * @since 2024-12-04
 */
@Service
public class SystemConfigService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    // 缓存配置项，避免每次都查询数据库
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    // 默认配置值
    private static final String DEFAULT_THRESHOLD_5_PERCENT = "5.0";
    private static final String DEFAULT_THRESHOLD_4_PERCENT = "4.0";
    private static final String DEFAULT_MONITOR_DAYS = "7";
    private static final String DEFAULT_SCHEDULE_CRON = "0 0 9 * * ?";
    
    /**
     * 初始化配置缓存
     */
    @PostConstruct
    public void initConfigCache() {
        loadAllConfigs();
    }
    
    /**
     * 加载所有启用的配置到缓存
     */
    public void loadAllConfigs() {
        try {
            List<SystemConfig> configs = systemConfigMapper.selectAllEnabled();
            configCache.clear();
            for (SystemConfig config : configs) {
                configCache.put(config.getConfigKey(), config.getConfigValue());
            }
            log.info("系统配置缓存加载完成，共加载 {} 个配置项", configs.size());
        } catch (Exception e) {
            log.error("加载系统配置缓存失败", e);
        }
    }
    
    /**
     * 获取5%阈值配置
     *
     * @return 5%阈值
     */
    public BigDecimal getThreshold5Percent() {
        String value = configCache.get("threshold_5_percent");
        if (value == null) {
            value = DEFAULT_THRESHOLD_5_PERCENT;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("5%阈值配置格式错误，使用默认值: {}", DEFAULT_THRESHOLD_5_PERCENT);
            return new BigDecimal(DEFAULT_THRESHOLD_5_PERCENT);
        }
    }
    
    /**
     * 获取4%阈值配置
     *
     * @return 4%阈值
     */
    public BigDecimal getThreshold4Percent() {
        String value = configCache.get("threshold_4_percent");
        if (value == null) {
            value = DEFAULT_THRESHOLD_4_PERCENT;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("4%阈值配置格式错误，使用默认值: {}", DEFAULT_THRESHOLD_4_PERCENT);
            return new BigDecimal(DEFAULT_THRESHOLD_4_PERCENT);
        }
    }
    
    /**
     * 获取监控天数配置
     *
     * @return 监控天数
     */
    public int getMonitorDays() {
        String value = configCache.get("monitor_days");
        if (value == null) {
            value = DEFAULT_MONITOR_DAYS;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("监控天数配置格式错误，使用默认值: {}", DEFAULT_MONITOR_DAYS);
            return Integer.parseInt(DEFAULT_MONITOR_DAYS);
        }
    }
    
    /**
     * 获取定时任务cron表达式
     *
     * @return cron表达式
     */
    public String getScheduleCron() {
        String value = configCache.get("schedule_cron");
        if (value == null) {
            value = DEFAULT_SCHEDULE_CRON;
        }
        return value;
    }
    
    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    public String getConfigValue(String configKey) {
        return configCache.get(configKey);
    }
    
    /**
     * 根据配置键获取配置值，如果不存在则返回默认值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = configCache.get(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 更新配置项
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    public boolean updateConfig(String configKey, String configValue) {
        try {
            SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
            if (config != null) {
                config.setConfigValue(configValue);
                int result = systemConfigMapper.update(config);
                if (result > 0) {
                    // 更新缓存
                    configCache.put(configKey, configValue);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("更新配置项失败，configKey: {}", configKey, e);
        }
        return false;
    }
    
    /**
     * 获取所有配置项
     *
     * @return 配置项列表
     */
    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectAll();
    }
    
    /**
     * 根据ID获取配置项
     *
     * @param id 配置项ID
     * @return 配置项
     */
    public SystemConfig getConfigById(Long id) {
        return systemConfigMapper.selectById(id);
    }
    
    /**
     * 更新配置项
     *
     * @param systemConfig 配置项对象
     * @return 是否更新成功
     */
    public boolean updateConfig(SystemConfig systemConfig) {
        try {
            int result = systemConfigMapper.update(systemConfig);
            if (result > 0) {
                // 更新缓存
                configCache.put(systemConfig.getConfigKey(), systemConfig.getConfigValue());
                return true;
            }
        } catch (Exception e) {
            log.error("更新配置项失败，id: {}", systemConfig.getId(), e);
        }
        return false;
    }
    
    /**
     * 删除配置项
     *
     * @param id 配置项ID
     * @return 是否删除成功
     */
    public boolean deleteConfig(Long id) {
        try {
            SystemConfig config = systemConfigMapper.selectById(id);
            if (config != null) {
                int result = systemConfigMapper.deleteById(id);
                if (result > 0) {
                    // 更新缓存
                    configCache.remove(config.getConfigKey());
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("删除配置项失败，id: {}", id, e);
        }
        return false;
    }
}