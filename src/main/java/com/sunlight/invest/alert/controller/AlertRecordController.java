package com.sunlight.invest.alert.controller;

import com.sunlight.invest.alert.entity.AlertRecord;
import com.sunlight.invest.alert.service.AlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 告警记录控制器
 *
 * @author System
 * @since 2024-12-03
 */
@Controller
@RequestMapping("/alert")
public class AlertRecordController {

    @Autowired
    private AlertRecordService alertRecordService;

    /**
     * 展示所有告警记录
     *
     * @param model Model对象
     * @return 告警记录页面
     */
    @GetMapping("/records")
    public String showAllAlertRecords(Model model) {
        List<AlertRecord> alertRecords = alertRecordService.getAllAlertRecords();
        model.addAttribute("alertRecords", alertRecords);
        return "alert-records";
    }

    /**
     * 根据ID查看告警记录详情
     *
     * @param id    告警记录ID
     * @param model Model对象
     * @return 告警记录详情页面
     */
    @GetMapping("/record/{id}")
    public String showAlertRecordDetail(@PathVariable Long id, Model model) {
        AlertRecord alertRecord = alertRecordService.getAlertRecordById(id);
        model.addAttribute("alertRecord", alertRecord);
        return "alert-record-detail";
    }
}