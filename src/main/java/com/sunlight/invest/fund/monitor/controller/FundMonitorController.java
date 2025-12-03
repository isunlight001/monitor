package com.sunlight.invest.fund.monitor.controller;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import com.sunlight.invest.fund.monitor.mapper.FundNavMapper;
import com.sunlight.invest.fund.monitor.service.FundCrawlerService;
import com.sunlight.invest.fund.monitor.service.FundMonitorService;
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

    @Autowired
    private FundCrawlerService fundCrawlerService;

    @Autowired
    private FundMonitorService fundMonitorService;

    @Autowired
    private FundNavMapper fundNavMapper;

    /**
     * 手动触发数据抓取
     *
     * @param fundCode  基金代码
     * @param fundName  基金名称
     * @param startDate 开始日期（可选，默认最近1个月）
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
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            int count = fundCrawlerService.crawlAndSave(fundCode, fundName, start, end);

            result.put("success", true);
            result.put("message", "数据抓取成功");
            result.put("count", count);
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
}
