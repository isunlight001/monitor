package com.sunlight.invest.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockDataTest {

    @Autowired
    private StockService stockService;

//    @PostConstruct
    public void testStockData() {
        try {
            System.out.println("=== 股票数据测试 ===");
            
            // 测试获取最近5天的数据
            List<StockData> recentData = stockService.getRecentStockData("000001", 5);
            System.out.println("最近5天的数据:");
            if (recentData != null && !recentData.isEmpty()) {
                for (StockData data : recentData) {
                    System.out.println(data);
                }
            } else {
                System.out.println("未获取到数据");
            }
            
            // 测试获取指定日期范围的数据
            List<StockData> historyData = stockService.getStockHistory("000001", "20251201", "20251210");
            System.out.println("指定日期范围的数据:");
            if (historyData != null && !historyData.isEmpty()) {
                for (StockData data : historyData) {
                    System.out.println(data);
                }
            } else {
                System.out.println("未获取到数据");
            }
            
            System.out.println("==================");
        } catch (Exception e) {
            System.err.println("测试股票数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}