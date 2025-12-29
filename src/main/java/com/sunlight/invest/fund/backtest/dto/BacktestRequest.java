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
     * 基金代码
     */
    private String fundCode;
    
    /**
     * 初始资金（元）
     */
    private double initialCapital = 100000.0;
    
    /**
     * 初始持仓比例（%）
     */
    private double initialHoldings = 50.0;
    
    /**
     * 加仓比例（%）
     */
    private double upPositionChange = 10.0;
    
    /**
     * 减仓比例（%）
     */
    private double downPositionChange = 10.0;
    
    /**
     * 涨幅阈值（%）
     */
    private double upThreshold = 2.0;
    
    /**
     * 跌幅阈值（%）
     */
    private double downThreshold = 0.5;
    
    /**
     * 规则A：连续上涨/下跌天数阈值
     */
    private int consecutiveDaysThreshold = 5;
    
    /**
     * 规则B：单日涨跌幅绝对值阈值（%）
     */
    private double singleDayThreshold = 5.0;
    
    /**
     * 规则C：连续2天累计涨跌幅绝对值阈值（%）
     */
    private double consecutive2DaysThreshold = 4.0;
    
    /**
     * 规则D：连续3天累计涨跌幅绝对值阈值（%）
     */
    private double consecutive3DaysThreshold = 5.0;
    
    /**
     * 规则E：连续4天累计涨跌幅绝对值阈值（%）
     */
    private double consecutive4DaysThreshold = 5.0;
    
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
    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

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

    public int getConsecutiveDaysThreshold() {
        return consecutiveDaysThreshold;
    }

    public void setConsecutiveDaysThreshold(int consecutiveDaysThreshold) {
        this.consecutiveDaysThreshold = consecutiveDaysThreshold;
    }

    public double getSingleDayThreshold() {
        return singleDayThreshold;
    }

    public void setSingleDayThreshold(double singleDayThreshold) {
        this.singleDayThreshold = singleDayThreshold;
    }

    public double getConsecutive2DaysThreshold() {
        return consecutive2DaysThreshold;
    }

    public void setConsecutive2DaysThreshold(double consecutive2DaysThreshold) {
        this.consecutive2DaysThreshold = consecutive2DaysThreshold;
    }

    public double getConsecutive3DaysThreshold() {
        return consecutive3DaysThreshold;
    }

    public void setConsecutive3DaysThreshold(double consecutive3DaysThreshold) {
        this.consecutive3DaysThreshold = consecutive3DaysThreshold;
    }

    public double getConsecutive4DaysThreshold() {
        return consecutive4DaysThreshold;
    }

    public void setConsecutive4DaysThreshold(double consecutive4DaysThreshold) {
        this.consecutive4DaysThreshold = consecutive4DaysThreshold;
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