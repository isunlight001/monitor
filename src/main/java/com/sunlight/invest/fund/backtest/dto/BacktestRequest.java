package com.sunlight.invest.fund.backtest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * 回测请求参数
 * 
 * @author system
 * @date 2025-12-01
 */
public class BacktestRequest {
    /**
     * 初始资金（元）
     */
    private double initialCapital = 100000.0;
    
    /**
     * 初始持仓（元）
     */
    private double initialHoldings = 100000.0;
    
    /**
     * 加仓金额（元）
     */
    private double upPositionChange = 10000.0;
    
    /**
     * 减仓金额（元）
     */
    private double downPositionChange = 10000.0;
    
    /**
     * 涨幅阈值（%）
     */
    private double upThreshold = 2.0;
    
    /**
     * 跌幅阈值（%）
     */
    private double downThreshold = 0.5;
    
    /**
     * 回测月数
     */
    private int backtestMonths = 12;
    
    /**
     * 回测开始日期（可选，优先使用日期区间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * 回测结束日期（可选，优先使用日期区间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // Getters and Setters
    public double getInitialCapital() {
        return initialCapital;
    }

    public void setInitialCapital(double initialCapital) {
        this.initialCapital = initialCapital;
    }

    public double getInitialHoldings() {
        return initialHoldings;
    }

    public void setInitialHoldings(double initialHoldings) {
        this.initialHoldings = initialHoldings;
    }

    public double getUpPositionChange() {
        return upPositionChange;
    }

    public void setUpPositionChange(double upPositionChange) {
        this.upPositionChange = upPositionChange;
    }

    public double getDownPositionChange() {
        return downPositionChange;
    }

    public void setDownPositionChange(double downPositionChange) {
        this.downPositionChange = downPositionChange;
    }

    public double getUpThreshold() {
        return upThreshold;
    }

    public void setUpThreshold(double upThreshold) {
        this.upThreshold = upThreshold;
    }

    public double getDownThreshold() {
        return downThreshold;
    }

    public void setDownThreshold(double downThreshold) {
        this.downThreshold = downThreshold;
    }

    public int getBacktestMonths() {
        return backtestMonths;
    }

    public void setBacktestMonths(int backtestMonths) {
        this.backtestMonths = backtestMonths;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
