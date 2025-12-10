package com.sunlight.invest.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class StockDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(StockDataFetcher.class);
    
    /**
     * 获取股票历史数据
     * @param stockCode 股票代码，如"000001"
     * @param startDate 开始日期，格式yyyyMMdd
     * @param endDate 结束日期，格式yyyyMMdd
     * @return 股票历史数据JSON字符串
     */
    public String fetchStockHistoryData(String stockCode, String startDate, String endDate) {
        try {
            String fullCode = "cn_" + stockCode;
            String urlStr = "https://q.stock.sohu.com/hisHq?code=" + fullCode + 
                           "&start=" + startDate + "&end=" + endDate +
                           "&stat=1&order=D&period=d&callback=historySearchHandler&rt=jsonp";
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            
            in.close();
            return response.toString();
        } catch (Exception e) {
            logger.error("获取股票数据失败: ", e);
            return null;
        }
    }
    
    /**
     * 获取最近N天的股票数据
     * @param stockCode 股票代码
     * @param days 天数
     * @return 股票历史数据
     */
    public String fetchRecentStockData(String stockCode, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
        
        return fetchStockHistoryData(stockCode, startDateStr, endDateStr);
    }
}