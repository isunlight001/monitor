package com.sunlight.invest.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayService {
    
    @Autowired
    private HolidayConfig holidayConfig;
    
    // 存储节假日的集合（可以根据实际情况从数据库或配置文件加载）
    private Set<LocalDate> holidays = new HashSet<>();
    
    public HolidayService() {
        // 初始化示例节假日
        initHolidays();
    }
    
    private void initHolidays() {
        // 示例节假日，实际应用中可以从数据库加载
        holidays.add(LocalDate.of(2025, 1, 1));   // 元旦
        holidays.add(LocalDate.of(2025, 2, 10));  // 春节
        holidays.add(LocalDate.of(2025, 2, 11));  // 春节
        holidays.add(LocalDate.of(2025, 2, 12));  // 春节
        holidays.add(LocalDate.of(2025, 4, 4));   // 清明节
        holidays.add(LocalDate.of(2025, 5, 1));   // 劳动节
        holidays.add(LocalDate.of(2025, 5, 2));   // 劳动节
        holidays.add(LocalDate.of(2025, 5, 3));   // 劳动节
        holidays.add(LocalDate.of(2025, 6, 14));  // 端午节
        holidays.add(LocalDate.of(2025, 9, 15));  // 中秋节
        holidays.add(LocalDate.of(2025, 10, 1));  // 国庆节
        holidays.add(LocalDate.of(2025, 10, 2));  // 国庆节
        holidays.add(LocalDate.of(2025, 10, 3));  // 国庆节
    }
    
    /**
     * 检查给定日期是否为节假日
     */
    public boolean isHoliday(LocalDate date) {
        // 检查是否为周末
        if (holidayConfig.isIncludeWeekends() && isWeekend(date)) {
            return true;
        }
        
        // 检查是否为配置的节假日
        return holidays.contains(date) || 
               (holidayConfig.getHolidayDates() != null && 
                holidayConfig.getHolidayDates().contains(date));
    }
    
    /**
     * 检查给定日期是否为周末
     */
    public boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * 添加节假日
     */
    public void addHoliday(LocalDate date) {
        holidays.add(date);
    }
    
    /**
     * 移除节假日
     */
    public void removeHoliday(LocalDate date) {
        holidays.remove(date);
    }
    
    /**
     * 重新加载节假日配置
     */
    public void reloadHolidays() {
        holidays.clear();
        initHolidays();
        
        // 添加配置文件中的节假日
        if (holidayConfig.getHolidayDates() != null) {
            holidays.addAll(holidayConfig.getHolidayDates());
        }
    }
}