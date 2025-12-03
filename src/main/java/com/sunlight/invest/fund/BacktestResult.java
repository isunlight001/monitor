package com.sunlight.invest.fund;

import java.time.LocalDate;

/**
 * 回测结果实体类
 */
public class BacktestResult {
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 基金单位净值
     */
    private double nav;
    
    /**
     * 基金总金额
     */
    private double fundAmount;
    
    /**
     * 操作类型
     */
    private String action;
    
    /**
     * 资金变化
     */
    private double change;
    
    public BacktestResult() {}
    
    public BacktestResult(LocalDate date, double nav, double fundAmount, String action, double change) {
        this.date = date;
        this.nav = nav;
        this.fundAmount = fundAmount;
        this.action = action;
        this.change = change;
    }
    
    // Getters and setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public double getNav() {
        return nav;
    }
    
    public void setNav(double nav) {
        this.nav = nav;
    }
    
    public double getFundAmount() {
        return fundAmount;
    }
    
    public void setFundAmount(double fundAmount) {
        this.fundAmount = fundAmount;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public double getChange() {
        return change;
    }
    
    public void setChange(double change) {
        this.change = change;
    }
    
    @Override
    public String toString() {
        return "BacktestResult{" +
                "date=" + date +
                ", nav=" + nav +
                ", fundAmount=" + fundAmount +
                ", action='" + action + '\'' +
                ", change=" + change +
                '}';
    }
}