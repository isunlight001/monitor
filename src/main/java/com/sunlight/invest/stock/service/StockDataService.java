package com.sunlight.invest.stock.service;

import com.sunlight.invest.stock.StockData;
import com.sunlight.invest.stock.entity.StockDataEntity;
import com.sunlight.invest.stock.mapper.StockDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockDataService {

    @Autowired
    private StockDataMapper stockDataMapper;

    /**
     * 初始化数据库表
     */
    @PostConstruct
    public void init() {
        try {
            stockDataMapper.createTable();
        } catch (Exception e) {
            // 表可能已经存在，忽略异常
        }
    }

    /**
     * 将StockData转换为StockDataEntity
     */
    public StockDataEntity convertToEntity(StockData stockData, String stockCode, String stockName) {
        StockDataEntity entity = new StockDataEntity();
        entity.setStockCode(stockCode);
        entity.setStockName(stockName);
        
        // 解析日期
        try {
            LocalDate tradeDate = LocalDate.parse(stockData.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            entity.setTradeDate(tradeDate);
        } catch (Exception e) {
            // 如果日期格式不匹配，尝试其他格式
            try {
                LocalDate tradeDate = LocalDate.parse(stockData.getDate(), DateTimeFormatter.BASIC_ISO_DATE);
                entity.setTradeDate(tradeDate);
            } catch (Exception ex) {
                // 如果都失败，使用当前日期
                entity.setTradeDate(LocalDate.now());
            }
        }
        
        entity.setOpenPrice(BigDecimal.valueOf(stockData.getOpenPrice()));
        entity.setClosePrice(BigDecimal.valueOf(stockData.getClosePrice()));
        entity.setHighPrice(BigDecimal.valueOf(stockData.getHighPrice()));
        entity.setLowPrice(BigDecimal.valueOf(stockData.getLowPrice()));
        entity.setVolume(0L); // 搜狐接口不提供成交量
        entity.setAmount(BigDecimal.ZERO); // 搜狐接口不提供成交额
        entity.setCreateTime(LocalDate.now());
        entity.setUpdateTime(LocalDate.now());
        
        return entity;
    }

    /**
     * 将StockDataEntity转换为StockData
     */
    public StockData convertToStockData(StockDataEntity entity) {
        StockData stockData = new StockData();
        stockData.setDate(entity.getTradeDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        stockData.setOpenPrice(entity.getOpenPrice().doubleValue());
        stockData.setClosePrice(entity.getClosePrice().doubleValue());
        stockData.setHighPrice(entity.getHighPrice().doubleValue());
        stockData.setLowPrice(entity.getLowPrice().doubleValue());
        return stockData;
    }

    /**
     * 保存股票数据到数据库
     */
    public void saveStockData(List<StockData> stockDataList, String stockCode, String stockName) {
        if (stockDataList == null || stockDataList.isEmpty()) {
            return;
        }

        List<StockDataEntity> entities = stockDataList.stream()
                .map(data -> convertToEntity(data, stockCode, stockName))
                .collect(Collectors.toList());

        // 批量插入数据
        try {
            stockDataMapper.batchInsert(entities);
        } catch (Exception e) {
            // 如果批量插入失败，逐个插入
            for (StockDataEntity entity : entities) {
                try {
                    stockDataMapper.insert(entity);
                } catch (Exception ex) {
                    // 忽略单个插入失败的情况（可能是重复数据）
                }
            }
        }
    }

    /**
     * 从数据库获取股票最近N天的数据
     */
    public List<StockData> getRecentStockDataFromDB(String stockCode, int days) {
        List<StockDataEntity> entities = stockDataMapper.selectRecentData(stockCode, days);
        return entities.stream()
                .map(this::convertToStockData)
                .collect(Collectors.toList());
    }

    /**
     * 从数据库获取指定日期范围的股票数据
     */
    public List<StockData> getStockDataByDateRangeFromDB(String stockCode, LocalDate startDate, LocalDate endDate) {
        List<StockDataEntity> entities = stockDataMapper.selectByDateRange(stockCode, startDate, endDate);
        return entities.stream()
                .map(this::convertToStockData)
                .collect(Collectors.toList());
    }

    /**
     * 检查指定日期的股票数据是否存在
     */
    public boolean isStockDataExists(String stockCode, LocalDate tradeDate) {
        StockDataEntity entity = stockDataMapper.selectByCodeAndDate(stockCode, tradeDate);
        return entity != null;
    }
}