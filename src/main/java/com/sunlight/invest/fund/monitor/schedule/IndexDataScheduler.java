package com.sunlight.invest.fund.monitor.schedule;

import com.sunlight.invest.fund.monitor.service.IndexDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 指数数据定时任务
 * <p>
 * 定期获取并更新各类指数数据
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
@Component
public class IndexDataScheduler {

    private static final Logger log = LoggerFactory.getLogger(IndexDataScheduler.class);

    @Autowired
    private IndexDataService indexDataService;

    /**
     * 每日16点执行指数数据更新任务
     * 获取当天的指数数据并保存到数据库
     */
    @Scheduled(cron = "0 0 16 * * ?")
    public void scheduledIndexDataUpdate() {
        log.info("开始执行指数数据更新任务");

        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(30); // 获取最近30天的数据
            
            int count = indexDataService.fetchAndSaveAllIndexData(startDate, today);
            
            log.info("指数数据更新任务完成，共更新 {} 条记录", count);
        } catch (Exception e) {
            log.error("指数数据更新任务执行失败: {}", e.getMessage(), e);
        }
    }
}