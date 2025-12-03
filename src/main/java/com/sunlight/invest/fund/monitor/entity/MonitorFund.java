package com.sunlight.invest.fund.monitor.entity;

import java.time.LocalDateTime;

/**
 * 监控基金实体类
 * <p>
 * 用于存储需要监控的基金列表
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
public class MonitorFund {
    
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
     * 是否启用监控
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // Constructors
    public MonitorFund() {}

    public MonitorFund(String fundCode, String fundName) {
        this.fundCode = fundCode;
        this.fundName = fundName;
        this.enabled = true;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
        return "MonitorFund{" +
                "id=" + id +
                ", fundCode='" + fundCode + '\'' +
                ", fundName='" + fundName + '\'' +
                ", enabled=" + enabled +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}