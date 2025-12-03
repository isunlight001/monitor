package com.sunlight.invest.alert.controller;

import com.sunlight.invest.alert.entity.AlertRecord;
import com.sunlight.invest.alert.service.AlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 获取所有告警记录
     *
     * @return 告警记录列表
     */
    @GetMapping("/records")
    public List<AlertRecord> getAllAlertRecords() {
        return alertRecordService.getAllAlertRecords();
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