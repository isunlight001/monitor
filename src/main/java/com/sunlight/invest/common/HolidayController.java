package com.sunlight.invest.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {
    
    @Autowired
    private HolidayService holidayService;
    
    /**
     * 检查指定日期是否为节假日
     */
    @GetMapping("/check")
    public Map<String, Object> isHoliday(@RequestParam("date") String dateStr) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            boolean isHoliday = holidayService.isHoliday(date);
            
            result.put("success", true);
            result.put("date", dateStr);
            result.put("isHoliday", isHoliday);
            result.put("message", isHoliday ? "是节假日" : "不是节假日");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "日期格式错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 添加节假日
     */
    @PostMapping("/add")
    public Map<String, Object> addHoliday(@RequestParam("date") String dateStr) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            holidayService.addHoliday(date);
            
            result.put("success", true);
            result.put("message", "节假日添加成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加节假日失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 移除节假日
     */
    @DeleteMapping("/remove")
    public Map<String, Object> removeHoliday(@RequestParam("date") String dateStr) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            holidayService.removeHoliday(date);
            
            result.put("success", true);
            result.put("message", "节假日移除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "移除节假日失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 重新加载节假日配置
     */
    @PostMapping("/reload")
    public Map<String, Object> reloadHolidays() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            holidayService.reloadHolidays();
            
            result.put("success", true);
            result.put("message", "节假日配置重新加载成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重新加载节假日配置失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取所有节假日
     */
    @GetMapping("/all")
    public Map<String, Object> getAllHolidays() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Set<LocalDate> holidays = holidayService.getAllHolidays();
            List<String> holidayStrings = holidays.stream()
                    .sorted()
                    .map(date -> date.toString())
                    .collect(Collectors.toList());
            
            result.put("success", true);
            result.put("data", holidayStrings);
            result.put("message", "获取节假日列表成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取节假日列表失败: " + e.getMessage());
        }
        
        return result;
    }
}