package com.sunlight.invest.fund.monitor.controller;

import com.sunlight.invest.fund.monitor.task.FundDataReportTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 基金报告控制器
 * 提供基金数据报告相关的API接口
 *
 * @author System
 * @since 2024-12-06
 */
@RestController
@RequestMapping("/api/fund-report")
public class FundReportController {

    private static final Logger log = LoggerFactory.getLogger(FundReportController.class);

    @Autowired
    private FundDataReportTask fundDataReportTask;

    /**
     * 手动触发发送基金数据报告
     */
    @PostMapping("/send")
    public Map<String, Object> sendFundReport() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("手动触发基金数据报告发送");
            
            // 执行发送基金数据报告任务
            fundDataReportTask.sendFundDataReport();
            
            result.put("success", true);
            result.put("message", "基金数据报告发送成功");
        } catch (Exception e) {
            log.error("手动触发基金数据报告发送失败", e);
            result.put("success", false);
            result.put("message", "基金数据报告发送失败: " + e.getMessage());
        }
        return result;
    }
}