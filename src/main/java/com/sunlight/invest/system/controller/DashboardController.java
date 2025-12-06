package com.sunlight.invest.system.controller;

import com.sunlight.invest.alert.entity.AlertRecord;
import com.sunlight.invest.alert.mapper.AlertRecordMapper;
import com.sunlight.invest.fund.monitor.mapper.MonitorFundMapper;
import com.sunlight.invest.notification.mapper.EmailRecipientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表板控制器
 * 提供首页统计数据和最近告警信息
 *
 * @author System
 * @since 2024-12-06
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private MonitorFundMapper monitorFundMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private EmailRecipientMapper emailRecipientMapper;

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取监控基金数量
            int fundCount = monitorFundMapper.countAll();
            
            // 获取今日告警数量
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            int todayAlerts = alertRecordMapper.countByDateRange(startOfDay, endOfDay);
            
            // 获取邮件接收人数量
            int recipientCount = emailRecipientMapper.countAll();
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("fundCount", fundCount);
            data.put("todayAlerts", todayAlerts);
            data.put("recipientCount", recipientCount);
            data.put("systemStatus", "🟢");
            
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取仪表板统计数据失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取最近告警记录
     */
    @GetMapping("/recent-alerts")
    public Map<String, Object> getRecentAlerts() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取最近5条告警记录
            List<AlertRecord> recentAlerts = alertRecordMapper.selectRecent(5);
            
            result.put("success", true);
            result.put("data", recentAlerts);
        } catch (Exception e) {
            log.error("获取最近告警记录失败", e);
            result.put("success", false);
            result.put("message", "获取告警记录失败: " + e.getMessage());
        }
        return result;
    }
}