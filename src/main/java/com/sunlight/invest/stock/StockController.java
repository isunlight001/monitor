package com.sunlight.invest.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    /**
     * 获取股票历史数据
     * @param stockCode 股票代码
     * @param startDate 开始日期 yyyyMMdd
     * @param endDate 结束日期 yyyyMMdd
     * @return 股票数据列表
     */
    @GetMapping("/history")
    public ApiResponse<List<StockData>> getStockHistory(
            @RequestParam String stockCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<StockData> stockData = stockService.getStockHistory(stockCode, startDate, endDate);
            return ApiResponse.success(stockData);
        } catch (Exception e) {
            return ApiResponse.error("获取股票数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最近N天的股票数据
     * @param stockCode 股票代码
     * @param days 天数
     * @return 股票数据列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<StockData>> getRecentStockData(
            @RequestParam String stockCode,
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<StockData> stockData = stockService.getRecentStockData(stockCode, days);
            return ApiResponse.success(stockData);
        } catch (Exception e) {
            return ApiResponse.error("获取股票数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取股票AI分析报告
     * @param stockCode 股票代码
     * @param days 天数
     * @return AI分析报告
     */
    @GetMapping("/analysis")
    public ApiResponse<String> getStockAnalysis(
            @RequestParam String stockCode,
            @RequestParam(defaultValue = "30") int days) {
        try {
            String analysis = stockService.getStockAnalysis(stockCode, days);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取AI分析失败: " + e.getMessage());
        }
    }
}