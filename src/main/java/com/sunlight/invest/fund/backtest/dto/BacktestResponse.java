package com.sunlight.invest.fund.backtest.dto;

import java.util.List;

/**
 * 回测响应结果
 * 
 * @author system
 * @date 2025-12-01
 */
public class BacktestResponse {
    /**
     * 初始资金
     */
    private double initialCapital;
    
    /**
     * 初始持仓
     */
    private double initialHoldings;
    
    /**
     * 最终资金
     */
    private double finalCapital;
    
    /**
     * 最终持仓份额
     */
    private double finalHoldings;
    
    /**
     * 最终净值
     */
    private double finalNav;
    
    /**
     * 最终持仓市值
     */
    private double finalHoldingsValue;
    
    /**
     * 总资产
     */
    private double totalAssets;
    
    /**
     * 总收益率（%）
     */
    private double returnRate;
    
    /**
     * 加仓次数
     */
    private int upPositionChanges;
    
    /**
     * 减仓次数
     */
    private int downPositionChanges;
    
    /**
     * 最大回撤（%）
     */
    private double maxDrawdown;
    
    /**
     * 持仓峰值
     */
    private double peakHoldings;
    
    /**
     * 交易日数量
     */
    private int tradingDays;
    
    /**
     * 每日明细
     */
    private List<DailyDetail> dailyDetails;

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

    public double getFinalCapital() {
        return finalCapital;
    }

    public void setFinalCapital(double finalCapital) {
        this.finalCapital = finalCapital;
    }

    public double getFinalHoldings() {
        return finalHoldings;
    }

    public void setFinalHoldings(double finalHoldings) {
        this.finalHoldings = finalHoldings;
    }

    public double getFinalNav() {
        return finalNav;
    }

    public void setFinalNav(double finalNav) {
        this.finalNav = finalNav;
    }

    public double getFinalHoldingsValue() {
        return finalHoldingsValue;
    }

    public void setFinalHoldingsValue(double finalHoldingsValue) {
        this.finalHoldingsValue = finalHoldingsValue;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(double totalAssets) {
        this.totalAssets = totalAssets;
    }

    public double getReturnRate() {
        return returnRate;
    }

    public void setReturnRate(double returnRate) {
        this.returnRate = returnRate;
    }

    public int getUpPositionChanges() {
        return upPositionChanges;
    }

    public void setUpPositionChanges(int upPositionChanges) {
        this.upPositionChanges = upPositionChanges;
    }

    public int getDownPositionChanges() {
        return downPositionChanges;
    }

    public void setDownPositionChanges(int downPositionChanges) {
        this.downPositionChanges = downPositionChanges;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public void setMaxDrawdown(double maxDrawdown) {
        this.maxDrawdown = maxDrawdown;
    }

    public double getPeakHoldings() {
        return peakHoldings;
    }

    public void setPeakHoldings(double peakHoldings) {
        this.peakHoldings = peakHoldings;
    }

    public int getTradingDays() {
        return tradingDays;
    }

    public void setTradingDays(int tradingDays) {
        this.tradingDays = tradingDays;
    }

    public List<DailyDetail> getDailyDetails() {
        return dailyDetails;
    }

    public void setDailyDetails(List<DailyDetail> dailyDetails) {
        this.dailyDetails = dailyDetails;
    }

    /**
     * 每日明细
     */
    public static class DailyDetail {
        private String date;
        private double indexChange;
        private double nav;
        private double capital;
        private double holdings;
        private double totalAssets;
        private String action;
        
        public DailyDetail(String date, double indexChange, double nav, double capital, 
                          double holdings, double totalAssets, String action) {
            this.date = date;
            this.indexChange = indexChange;
            this.nav = nav;
            this.capital = capital;
            this.holdings = holdings;
            this.totalAssets = totalAssets;
            this.action = action;
        }

        // Getters and Setters
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getIndexChange() {
            return indexChange;
        }

        public void setIndexChange(double indexChange) {
            this.indexChange = indexChange;
        }

        public double getNav() {
            return nav;
        }

        public void setNav(double nav) {
            this.nav = nav;
        }

        public double getCapital() {
            return capital;
        }

        public void setCapital(double capital) {
            this.capital = capital;
        }

        public double getHoldings() {
            return holdings;
        }

        public void setHoldings(double holdings) {
            this.holdings = holdings;
        }

        public double getTotalAssets() {
            return totalAssets;
        }

        public void setTotalAssets(double totalAssets) {
            this.totalAssets = totalAssets;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
