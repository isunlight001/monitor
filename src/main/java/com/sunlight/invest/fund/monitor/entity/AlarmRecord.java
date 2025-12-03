package com.sunlight.invest.fund.monitor.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 告警记录实体类
 *
 * @author System
 * @since 2024-12-03
 */
public class AlarmRecord {
    private Long id;
    private String fundCode;
    private String fundName;
    private String ruleCode; // A,B,C,D,E等
    private String ruleDescription;
    private Integer consecutiveDays;
    private BigDecimal cumulativeReturn;
    private BigDecimal dailyReturn;
    private LocalDate navDate;
    private BigDecimal unitNav;
    private String alarmContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public Integer getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public BigDecimal getCumulativeReturn() {
        return cumulativeReturn;
    }

    public void setCumulativeReturn(BigDecimal cumulativeReturn) {
        this.cumulativeReturn = cumulativeReturn;
    }

    public BigDecimal getDailyReturn() {
        return dailyReturn;
    }

    public void setDailyReturn(BigDecimal dailyReturn) {
        this.dailyReturn = dailyReturn;
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

    public String getAlarmContent() {
        return alarmContent;
    }

    public void setAlarmContent(String alarmContent) {
        this.alarmContent = alarmContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "AlarmRecord{" +
                "id=" + id +
                ", fundCode='" + fundCode + '\'' +
                ", fundName='" + fundName + '\'' +
                ", ruleCode='" + ruleCode + '\'' +
                ", ruleDescription='" + ruleDescription + '\'' +
                ", consecutiveDays=" + consecutiveDays +
                ", cumulativeReturn=" + cumulativeReturn +
                ", dailyReturn=" + dailyReturn +
                ", navDate=" + navDate +
                ", unitNav=" + unitNav +
                ", alarmContent='" + alarmContent + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}