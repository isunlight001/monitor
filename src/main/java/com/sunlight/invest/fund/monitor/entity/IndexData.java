package com.sunlight.invest.fund.monitor.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 指数数据实体类
 * <p>
 * 用于存储各类指数的每日数据，包括涨跌幅、最高点等信息
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
public class IndexData {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 指数代码
     */
    private String indexCode;
    
    /**
     * 指数名称
     */
    private String indexName;
    
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 开盘价
     */
    private BigDecimal openPrice;
    
    /**
     * 收盘价
     */
    private BigDecimal closePrice;
    
    /**
     * 最高价
     */
    private BigDecimal highPrice;
    
    /**
     * 最低价
     */
    private BigDecimal lowPrice;
    
    /**
     * 日涨跌幅（百分比）
     */
    private BigDecimal dailyReturn;
    
    /**
     * 成交量
     */
    private Long volume;
    
    /**
     * 成交额
     */
    private BigDecimal amount;
    
    /**
     * 创建时间
     */
    private LocalDate createTime;
    
    /**
     * 更新时间
     */
    private LocalDate updateTime;

    // Constructors
    public IndexData() {}

    public IndexData(String indexCode, String indexName, LocalDate tradeDate, BigDecimal openPrice, 
                     BigDecimal closePrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal dailyReturn) {
        this.indexCode = indexCode;
        this.indexName = indexName;
        this.tradeDate = tradeDate;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.dailyReturn = dailyReturn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndexCode() {
        return indexCode;
    }

    public void setIndexCode(String indexCode) {
        this.indexCode = indexCode;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getDailyReturn() {
        return dailyReturn;
    }

    public void setDailyReturn(BigDecimal dailyReturn) {
        this.dailyReturn = dailyReturn;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDate createTime) {
        this.createTime = createTime;
    }

    public LocalDate getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDate updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "IndexData{" +
                "id=" + id +
                ", indexCode='" + indexCode + '\'' +
                ", indexName='" + indexName + '\'' +
                ", tradeDate=" + tradeDate +
                ", openPrice=" + openPrice +
                ", closePrice=" + closePrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                ", dailyReturn=" + dailyReturn +
                ", volume=" + volume +
                ", amount=" + amount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}