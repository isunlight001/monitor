package com.sunlight.invest.fund;

import java.time.LocalDate;

/**
 * 基金数据实体类
 */
public class Fund {
    /**
     * 交易日期
     */
    private LocalDate tradeDate;
    
    /**
     * 单位净值
     */
    private double nav;
    
    public Fund() {}
    
    public Fund(LocalDate tradeDate, double nav) {
        this.tradeDate = tradeDate;
        this.nav = nav;
    }
    
    // Getters and setters
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public double getNav() {
        return nav;
    }
    
    public void setNav(double nav) {
        this.nav = nav;
    }
    
    @Override
    public String toString() {
        return "Fund{" +
                "tradeDate=" + tradeDate +
                ", nav=" + nav +
                '}';
    }
}