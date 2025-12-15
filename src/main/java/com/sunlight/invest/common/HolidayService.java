package com.sunlight.invest.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
public class HolidayService {
    
    @Autowired
    private HolidayConfig holidayConfig;
    
    // 存储节假日的集合（可以根据实际情况从数据库或配置文件加载）
    private Set<LocalDate> holidays = new HashSet<>();
    
    // 节假日文件路径
    private static final String HOLIDAY_FILE_PATH = "src/main/resources/data/holidays.txt";
    
    public HolidayService() {
        // 初始化示例节假日
        initHolidays();
    }
    
    @PostConstruct
    public void postConstruct() {
        // 从文件加载节假日
        loadHolidaysFromFile();
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
        
        // 保存到文件
        saveHolidaysToFile();
    }
    
    /**
     * 从文件加载节假日
     */
    private void loadHolidaysFromFile() {
        try {
            Path path = Paths.get(HOLIDAY_FILE_PATH);
            if (Files.exists(path)) {
                BufferedReader reader = Files.newBufferedReader(path);
                String line;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                while ((line = reader.readLine()) != null) {
                    try {
                        LocalDate date = LocalDate.parse(line.trim(), formatter);
                        holidays.add(date);
                    } catch (Exception e) {
                        // 忽略无效日期
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            // 文件不存在或读取失败，使用默认节假日
        }
    }
    
    /**
     * 将节假日保存到文件
     */
    private void saveHolidaysToFile() {
        try {
            Path path = Paths.get(HOLIDAY_FILE_PATH);
            // 确保父目录存在
            Files.createDirectories(path.getParent());
            
            BufferedWriter writer = Files.newBufferedWriter(path);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (LocalDate date : holidays) {
                writer.write(date.format(formatter));
                writer.newLine();
            }
            
            writer.close();
        } catch (Exception e) {
            // 保存失败，不影响程序运行
            e.printStackTrace();
        }
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
        saveHolidaysToFile(); // 保存到文件
    }
    
    /**
     * 移除节假日
     */
    public void removeHoliday(LocalDate date) {
        holidays.remove(date);
        saveHolidaysToFile(); // 保存到文件
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
    
    /**
     * 获取所有节假日
     */
    public Set<LocalDate> getAllHolidays() {
        return new HashSet<>(holidays);
    }
}