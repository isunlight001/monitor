package com.sunlight.invest.stock;

import com.sunlight.ai.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {
    
    @Autowired
    private StockDataFetcher stockDataFetcher;
    
    @Autowired
    private StockDataParser stockDataParser;
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    /**
     * 获取股票历史数据
     * @param stockCode 股票代码
     * @param startDate 开始日期 yyyyMMdd
     * @param endDate 结束日期 yyyyMMdd
     * @return 股票数据列表
     */
    public List<StockData> getStockHistory(String stockCode, String startDate, String endDate) {
        String jsonData = stockDataFetcher.fetchStockHistoryData(stockCode, startDate, endDate);
        return stockDataParser.parseStockData(jsonData);
    }
    
    /**
     * 获取最近N天的股票数据
     * @param stockCode 股票代码
     * @param days 天数
     * @return 股票数据列表
     */
    public List<StockData> getRecentStockData(String stockCode, int days) {
        String jsonData = stockDataFetcher.fetchRecentStockData(stockCode, days);
        return stockDataParser.parseStockData(jsonData);
    }
    
    /**
     * 获取股票AI分析报告
     * @param stockCode 股票代码
     * @param days 天数
     * @return AI分析报告
     */
    public String getStockAnalysis(String stockCode, int days) {
        // 获取股票数据
        List<StockData> stockDataList = getRecentStockData(stockCode, days);
        
        if (stockDataList == null || stockDataList.isEmpty()) {
            return "无法获取股票数据";
        }
        
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("角色：你是一名经验丰富的股票投资分析专家，擅长技术面和基本面结合分析。\n");
        prompt.append("任务：请分析以下A股股票的历史数据。数据格式为：每行包含\"交易日期、开盘价、收盘价、最高价、最低价\"。\n");
        prompt.append("数据：\n");
        
        // 添加数据
        for (StockData data : stockDataList) {
            prompt.append(data.getDate()).append("、")
                  .append(data.getOpenPrice()).append("、")
                  .append(data.getClosePrice()).append("、")
                  .append(data.getHighPrice()).append("、")
                  .append(data.getLowPrice()).append("\n");
        }
        
        prompt.append("要求：请按以下结构输出分析报告：\n");
        prompt.append("趋势判断：当前处于上升、下降还是震荡趋势？\n");
        prompt.append("关键位置：近期的支撑位和压力位大致在哪里？\n");
        prompt.append("风险提示：当前主要的风险点是什么？\n");
        prompt.append("操作建议：给出短期（1-3天）的操作策略建议（如观望、分批建仓、减仓）及理由。\n");
        
        // 调用AI服务
        return deepSeekService.getAIResponse(prompt.toString());
    }
}