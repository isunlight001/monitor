package com.sunlight.invest.test;

import java.time.LocalDate;

/**
 * 指数数据实体类
 */
public class IndexData {
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 上证指数涨跌幅
     */
    private Double shPercent;
    
    /**
     * 深证成指涨跌幅
     */
    private Double szPercent;
    
    /**
     * 创业板指涨跌幅
     */
    private Double chuangyePercent;
    
    public IndexData() {}
    
    public IndexData(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    // Getters and setters
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public Double getShPercent() {
        return shPercent;
    }
    
    public void setShPercent(Double shPercent) {
        this.shPercent = shPercent;
    }
    
    public Double getSzPercent() {
        return szPercent;
    }
    
    public void setSzPercent(Double szPercent) {
        this.szPercent = szPercent;
    }
    
    public Double getChuangyePercent() {
        return chuangyePercent;
    }
    
    public void setChuangyePercent(Double chuangyePercent) {
        this.chuangyePercent = chuangyePercent;
    }
    
    @Override
    public String toString() {
        return "IndexData{" +
                "tradeDate=" + tradeDate +
                ", shPercent=" + shPercent +
                ", szPercent=" + szPercent +
                ", chuangyePercent=" + chuangyePercent +
                '}';
    }
}