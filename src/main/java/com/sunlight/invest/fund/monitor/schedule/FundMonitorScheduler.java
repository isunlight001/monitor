package com.sunlight.invest.fund.monitor.schedule;


import com.sunlight.invest.fund.monitor.service.FundMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


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
    private FundMonitorService fundMonitorService;


    /**
     * 每天8点执行基金数据抓取和监控
     * cron表达式: 0 0 22 * * ?
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledMonitorTask() {
        log.info("========== 开始执行基金监控定时任务 ==========");
        fundMonitorService.scheduledMonitorTask();
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
