package com.sunlight.invest.alert.controller;

import com.sunlight.invest.alert.entity.AlertRecord;
import com.sunlight.invest.alert.service.AlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警记录API控制器
 *
 * @author System
 * @since 2024-12-03
 */
@RestController
@RequestMapping("/api/alert")
public class AlertApiController {

    @Autowired
    private AlertRecordService alertRecordService;

    /**
     * 获取所有告警记录（支持分页）
     *
     * @param page 页码（从1开始，默认为1）
     * @param size 每页大小（默认为20）
     * @return 告警记录列表和分页信息
     */
    @GetMapping("/records")
    public Map<String, Object> getAllAlertRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取分页数据
            List<AlertRecord> records = alertRecordService.getAlertRecordsWithPagination(page, size);
            
            // 获取总记录数
            int totalCount = alertRecordService.getTotalAlertRecordCount();
            
            // 计算分页信息
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            result.put("success", true);
            result.put("data", records);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalRecords", totalCount);
            result.put("totalPages", totalPages);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取告警记录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据ID获取告警记录
     *
     * @param id 告警记录ID
     * @return 告警记录实体
     */
    @GetMapping("/record/{id}")
    public AlertRecord getAlertRecordById(@PathVariable Long id) {
        return alertRecordService.getAlertRecordById(id);
    }
}