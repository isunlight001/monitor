package com.sunlight.invest.fund.monitor.schedule;

import com.sunlight.invest.fund.monitor.service.FundCrawlerService;
import com.sunlight.invest.fund.monitor.service.FundMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 基金监控定时任务
 * <p>
 * 每晚11点执行基金数据抓取和监控
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@Component
public class FundMonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(FundMonitorScheduler.class);

    @Autowired
    private FundCrawlerService fundCrawlerService;

    @Autowired
    private FundMonitorService fundMonitorService;

    /**
     * 监控的基金列表（基金代码:基金名称）
     * 可以通过配置文件配置
     */
    @Value("${fund.monitor.codes:006195:国金量化多因子}")
    private String monitorFundsConfig;

    /**
     * 每晚11点执行基金数据抓取和监控
     * cron表达式: 0 0 23 * * ?
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduledMonitorTask() {
        log.info("========== 开始执行基金监控定时任务 ==========");

        List<String> fundConfigs = Arrays.asList(monitorFundsConfig.split(","));

        for (String fundConfig : fundConfigs) {
            String[] parts = fundConfig.trim().split(":");
            if (parts.length != 2) {
                log.warn("基金配置格式错误: {}", fundConfig);
                continue;
            }

            String fundCode = parts[0].trim();
            String fundName = parts[1].trim();

            try {
                // 1. 增量更新基金数据
                log.info("开始更新基金数据: {} - {}", fundCode, fundName);
                int updateCount = fundCrawlerService.incrementalUpdate(fundCode, fundName);
                log.info("基金数据更新完成: {} - {}, 更新记录数: {}", fundCode, fundName, updateCount);

                // 2. 执行监控检查
                log.info("开始监控基金: {} - {}", fundCode, fundName);
                fundMonitorService.monitorFund(fundCode);
                log.info("基金监控完成: {} - {}", fundCode, fundName);

            } catch (Exception e) {
                log.error("处理基金失败: {} - {}", fundCode, fundName, e);
            }
        }

        log.info("========== 基金监控定时任务执行完成 ==========");
    }

    /**
     * 手动触发监控任务（用于测试）
     * 每5分钟执行一次（仅用于开发测试）
     * 生产环境应注释掉此方法
     */
    // @Scheduled(fixedRate = 300000) // 5分钟
    public void manualTriggerTask() {
        log.info("手动触发基金监控任务");
        scheduledMonitorTask();
    }
}
