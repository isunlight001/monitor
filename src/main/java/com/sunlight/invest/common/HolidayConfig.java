package com.sunlight.invest.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "app.holidays")
public class HolidayConfig {
    
    /**
     * 节假日列表，格式为 yyyy-MM-dd
     */
    private List<String> dates;
    
    /**
     * 是否包含周末
     */
    private boolean includeWeekends = true;
    
    public List<String> getDates() {
        return dates;
    }
    
    public void setDates(List<String> dates) {
        this.dates = dates;
    }
    
    public boolean isIncludeWeekends() {
        return includeWeekends;
    }
    
    public void setIncludeWeekends(boolean includeWeekends) {
        this.includeWeekends = includeWeekends;
    }
    
    /**
     * 获取节假日日期列表
     */
    public List<LocalDate> getHolidayDates() {
        if (dates == null) {
            return Collections.emptyList();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dates.stream()
                .map(dateStr -> LocalDate.parse(dateStr, formatter))
                .collect(Collectors.toList());
    }
}