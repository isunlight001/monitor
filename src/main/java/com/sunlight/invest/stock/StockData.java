package com.sunlight.invest.stock;

public class StockData {
    private String date;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    
    // 构造函数
    public StockData() {}
    
    public StockData(String date, double openPrice, double closePrice, double highPrice, double lowPrice) {
        this.date = date;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }
    
    // Getter和Setter方法
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public double getOpenPrice() {
        return openPrice;
    }
    
    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }
    
    public double getClosePrice() {
        return closePrice;
    }
    
    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }
    
    public double getHighPrice() {
        return highPrice;
    }
    
    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }
    
    public double getLowPrice() {
        return lowPrice;
    }
    
    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }
    
    @Override
    public String toString() {
        return "StockData{" +
                "date='" + date + '\'' +
                ", openPrice=" + openPrice +
                ", closePrice=" + closePrice +
                ", highPrice=" + highPrice +
                ", lowPrice=" + lowPrice +
                '}';
    }
}