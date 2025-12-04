package com.sunlight.invest.fund.monitor.controller;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.entity.MonitorFund;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.mapper.MonitorFundMapper;
import com.sunlight.invest.fund.monitor.service.FundCrawlerService;
import com.sunlight.invest.fund.monitor.service.FundMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基金监控控制器
 * <p>
 * 提供基金监控相关的API接口
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@RestController
@RequestMapping("/api/fund/monitor")
@CrossOrigin(origins = "*")
public class FundMonitorController {
    
    private static final Logger log = LoggerFactory.getLogger(FundMonitorController.class);

    @Autowired
    private FundCrawlerService fundCrawlerService;

    @Autowired
    private FundMonitorService fundMonitorService;

    @Autowired
    private FundNavMapper fundNavMapper;

    @Autowired
    private MonitorFundMapper monitorFundMapper;

    /**
     * 手动触发数据抓取
     *
     * @param fundCode  基金代码
     * @param fundName  基金名称
     * @param startDate 开始日期（可选，默认最近20年）
     * @param endDate   结束日期（可选，默认今天）
     * @return 响应结果
     */
    @PostMapping("/crawl")
    public Map<String, Object> crawlFundData(
            @RequestParam String fundCode,
            @RequestParam String fundName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusYears(20);
            
            // 按年分批抓取数据，每次抓取一年的数据
            int totalCount = 0;
            LocalDate currentEnd = end;
            
            // 循环20次，每次处理一年的数据
            for (int year = 0; year < 20 && !currentEnd.isBefore(start); year++) {
                LocalDate currentStart = currentEnd.minusYears(1).plusDays(1);
                // 确保不超出起始日期
                if (currentStart.isBefore(start)) {
                    currentStart = start;
                }
                
                log.info("开始抓取基金数据: fundCode={}, fundName={}, startDate={}, endDate={}", 
                        fundCode, fundName, currentStart, currentEnd);
                
                int count = fundCrawlerService.crawlAndSave(fundCode, fundName, currentStart, currentEnd);
                totalCount += count;
                
                log.info("完成抓取基金数据: fundCode={}, fundName={}, startDate={}, endDate={}, count={}", 
                        fundCode, fundName, currentStart, currentEnd, count);
                
                // 更新下一批次的时间范围
                currentEnd = currentStart.minusDays(1);
                
                // 添加短暂延迟，避免请求过于频繁
                Thread.sleep(1000);
            }

            result.put("success", true);
            result.put("message", "数据抓取成功，总共更新记录数: " + totalCount);
            result.put("count", totalCount);
            result.put("fundCode", fundCode);
            result.put("fundName", fundName);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据抓取失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 手动触发监控检查
     *
     * @param fundCode 基金代码
     * @return 响应结果
     */
    @PostMapping("/check")
    public Map<String, Object> checkFund(@RequestParam String fundCode) {
        Map<String, Object> result = new HashMap<>();

        try {
            fundMonitorService.monitorFund(fundCode);

            result.put("success", true);
            result.put("message", "监控检查完成");
            result.put("fundCode", fundCode);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "监控检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询基金最近的净值数据
     *
     * @param fundCode 基金代码
     * @param days     天数（默认30天）
     * @return 基金净值列表
     */
    @GetMapping("/nav")
    public Map<String, Object> getFundNav(
            @RequestParam String fundCode,
            @RequestParam(defaultValue = "30") int days) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<FundNav> navList = fundNavMapper.selectRecentDays(fundCode, days);

            result.put("success", true);
            result.put("fundCode", fundCode);
            result.put("count", navList.size());
            result.put("data", navList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 增量更新基金数据
     *
     * @param fundCode 基金代码
     * @param fundName 基金名称
     * @return 响应结果
     */
    @PostMapping("/update")
    public Map<String, Object> incrementalUpdate(
            @RequestParam String fundCode,
            @RequestParam String fundName) {

        Map<String, Object> result = new HashMap<>();

        try {
            int count = fundCrawlerService.incrementalUpdate(fundCode, fundName);

            result.put("success", true);
            result.put("message", "增量更新成功");
            result.put("count", count);
            result.put("fundCode", fundCode);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "增量更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 添加监控基金
     *
     * @param fundCode 基金代码
     * @param fundName 基金名称
     * @return 响应结果
     */
    @PostMapping("/monitor-fund")
    public Map<String, Object> addMonitorFund(
            @RequestParam String fundCode,
            @RequestParam String fundName) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 检查是否已存在
            MonitorFund existing = monitorFundMapper.selectByFundCode(fundCode);
            if (existing != null) {
                result.put("success", false);
                result.put("message", "基金已在监控列表中");
                return result;
            }

            // 添加到监控列表
            MonitorFund monitorFund = new MonitorFund(fundCode, fundName);
            int count = monitorFundMapper.insert(monitorFund);

            result.put("success", true);
            result.put("message", "添加成功");
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 查询所有监控基金
     *
     * @return 监控基金列表
     */
    @GetMapping("/monitor-funds")
    public Map<String, Object> getAllMonitorFunds() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<MonitorFund> funds = monitorFundMapper.selectAll();

            result.put("success", true);
            result.put("count", funds.size());
            result.put("data", funds);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新监控基金状态
     *
     * @param id      基金ID
     * @param enabled 是否启用
     * @return 响应结果
     */
    @PutMapping("/monitor-fund/{id}/status")
    public Map<String, Object> updateMonitorFundStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {

        Map<String, Object> result = new HashMap<>();

        try {
            MonitorFund monitorFund = monitorFundMapper.selectById(id);
            if (monitorFund == null) {
                result.put("success", false);
                result.put("message", "基金不存在");
                return result;
            }

            monitorFund.setEnabled(enabled);
            int count = monitorFundMapper.update(monitorFund);

            result.put("success", true);
            result.put("message", "更新成功");
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除监控基金
     *
     * @param id 基金ID
     * @return 响应结果
     */
    @DeleteMapping("/monitor-fund/{id}")
    public Map<String, Object> deleteMonitorFund(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            int count = monitorFundMapper.deleteById(id);

            result.put("success", true);
            result.put("message", "删除成功");
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 手动触发所有监控基金的预警任务
     *
     * @return 响应结果
     */
    @PostMapping("/trigger-all")
    public Map<String, Object> triggerAllMonitorFunds() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用定时任务方法来执行所有基金的监控
            fundMonitorService.scheduledMonitorTask();

            result.put("success", true);
            result.put("message", "预警任务已触发，请查看日志了解执行详情");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "触发预警任务失败: " + e.getMessage());
        }

        return result;
    }
}
