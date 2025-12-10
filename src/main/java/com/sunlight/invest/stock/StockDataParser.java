package com.sunlight.invest.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StockDataParser {
    private static final Logger logger = LoggerFactory.getLogger(StockDataParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析股票数据
     * @param jsonData 从搜狐接口获取的原始JSON数据
     * @return 解析后的股票数据列表
     */
    public List<StockData> parseStockData(String jsonData) {
        List<StockData> stockDataList = new ArrayList<>();
        
        // 处理null或空数据
        if (jsonData == null || jsonData.isEmpty()) {
            logger.warn("收到空的JSON数据");
            return stockDataList;
        }
        
        try {
            // 移除JSONP包装
            String jsonStr = jsonData;
            if (jsonData.startsWith("historySearchHandler(") && jsonData.endsWith(")")) {
                jsonStr = jsonData.substring(21, jsonData.length() - 1);
            }
            
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            
            // 检查是否有数据
            if (rootNode != null && rootNode.isArray() && rootNode.size() > 0) {
                JsonNode dataNode = rootNode.get(0);
                if (dataNode != null) {
                    // 检查状态码
                    JsonNode statusNode = dataNode.get("status");
                    if (statusNode != null && statusNode.asInt() != 0) {
                        logger.warn("股票数据接口返回错误状态: {}", statusNode.asInt());
                        return stockDataList;
                    }
                    
                    JsonNode hqNode = dataNode.get("hq");
                    
                    if (hqNode != null && hqNode.isArray()) {
                        // 解析每条记录
                        for (JsonNode record : hqNode) {
                            if (record != null && record.isArray() && record.size() >= 5) {
                                StockData stockData = new StockData();
                                stockData.setDate(record.get(0).asText());
                                
                                // 解析价格数据，去除百分比符号
                                stockData.setOpenPrice(parseDoubleValue(record.get(1).asText()));
                                stockData.setClosePrice(parseDoubleValue(record.get(2).asText()));
                                stockData.setHighPrice(parseDoubleValue(record.get(3).asText()));
                                stockData.setLowPrice(parseDoubleValue(record.get(4).asText()));
                                
                                stockDataList.add(stockData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析股票数据失败: ", e);
        }
        
        return stockDataList;
    }
    
    /**
     * 解析double值，去除可能的百分比符号或其他非数字字符
     * @param value 字符串值
     * @return 解析后的double值
     */
    private double parseDoubleValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        
        // 去除百分比符号和其他非数字字符，只保留数字、小数点、负号
        String cleanValue = value.replaceAll("[^\\d.-]", "");
        
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            logger.warn("无法解析数值: {}, 原始值: {}", cleanValue, value);
            return 0.0;
        }
    }
}