package com.sunlight.invest.fund.monitor.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 基金净值实体类
 * <p>
 * 用于存储基金每日净值数据
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
public class FundNav {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 基金代码
     */
    private String fundCode;
    
    /**
     * 基金名称
     */
    private String fundName;
    
    /**
     * 净值日期
     */
    private LocalDate navDate;
    
    /**
     * 单位净值
     */
    private BigDecimal unitNav;
    
    /**
     * 日涨跌幅（百分比）
     */
    private BigDecimal dailyReturn;
    
    /**
     * 创建时间
     */
    private LocalDate createTime;
    
    /**
     * 更新时间
     */
    private LocalDate updateTime;

    // Constructors
    public FundNav() {}

    public FundNav(String fundCode, String fundName, LocalDate navDate, BigDecimal unitNav, BigDecimal dailyReturn) {
        this.fundCode = fundCode;
        this.fundName = fundName;
        this.navDate = navDate;
        this.unitNav = unitNav;
        this.dailyReturn = dailyReturn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public LocalDate getNavDate() {
        return navDate;
    }

    public void setNavDate(LocalDate navDate) {
        this.navDate = navDate;
    }

    public BigDecimal getUnitNav() {
        return unitNav;
    }

    public void setUnitNav(BigDecimal unitNav) {
        this.unitNav = unitNav;
    }

    public BigDecimal getDailyReturn() {
        return dailyReturn;
    }

    public void setDailyReturn(BigDecimal dailyReturn) {
        this.dailyReturn = dailyReturn;
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
        return "FundNav{" +
                "id=" + id +
                ", fundCode='" + fundCode + '\'' +
                ", fundName='" + fundName + '\'' +
                ", navDate=" + navDate +
                ", unitNav=" + unitNav +
                ", dailyReturn=" + dailyReturn +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
