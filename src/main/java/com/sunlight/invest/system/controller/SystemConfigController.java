package com.sunlight.invest.system.controller;

import com.sunlight.invest.system.entity.SystemConfig;
import com.sunlight.invest.system.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 *
 * @author System
 * @since 2024-12-04
 */
@RestController
@RequestMapping("/api/system/config")
@CrossOrigin(origins = "*")
public class SystemConfigController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemConfigController.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取所有配置项
     *
     * @return 配置项列表
     */
    @GetMapping
    public Map<String, Object> getAllConfigs() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<SystemConfig> configs = systemConfigService.getAllConfigs();
            
            result.put("success", true);
            result.put("data", configs);
            result.put("count", configs.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取配置项失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据ID获取配置项
     *
     * @param id 配置项ID
     * @return 配置项
     */
    @GetMapping("/{id}")
    public Map<String, Object> getConfigById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            SystemConfig config = systemConfigService.getConfigById(id);
            
            if (config != null) {
                result.put("success", true);
                result.put("data", config);
            } else {
                result.put("success", false);
                result.put("message", "配置项不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取配置项失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 更新配置项
     *
     * @param systemConfig 配置项对象
     * @return 更新结果
     */
    @PutMapping
    public Map<String, Object> updateConfig(@RequestBody SystemConfig systemConfig) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = systemConfigService.updateConfig(systemConfig);
            
            if (success) {
                result.put("success", true);
                result.put("message", "配置项更新成功");
            } else {
                result.put("success", false);
                result.put("message", "配置项更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置项更新失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除配置项
     *
     * @param id 配置项ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteConfig(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = systemConfigService.deleteConfig(id);
            
            if (success) {
                result.put("success", true);
                result.put("message", "配置项删除成功");
            } else {
                result.put("success", false);
                result.put("message", "配置项删除失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "配置项删除失败: " + e.getMessage());
        }
        
        return result;
    }
}