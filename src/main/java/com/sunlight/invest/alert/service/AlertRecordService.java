package com.sunlight.invest.alert.service;

import com.sunlight.invest.alert.entity.AlertRecord;
import com.sunlight.invest.alert.mapper.AlertRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警记录服务类
 *
 * @author System
 * @since 2024-12-03
 */
@Service
public class AlertRecordService {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    /**
     * 保存告警记录
     *
     * @param alertRecord 告警记录实体
     * @return 保存的记录数
     */
    public int saveAlertRecord(AlertRecord alertRecord) {
        return alertRecordMapper.insert(alertRecord);
    }

    /**
     * 获取所有告警记录
     *
     * @return 告警记录列表
     */
    public List<AlertRecord> getAllAlertRecords() {
        return alertRecordMapper.selectAll();
    }

    /**
     * 根据ID获取告警记录
     *
     * @param id 告警记录ID
     * @return 告警记录实体
     */
    public AlertRecord getAlertRecordById(Long id) {
        return alertRecordMapper.selectById(id);
    }

    /**
     * 根据基金代码获取告警记录
     *
     * @param fundCode 基金代码
     * @return 告警记录列表
     */
    public List<AlertRecord> getAlertRecordsByFundCode(String fundCode) {
        return alertRecordMapper.selectByFundCode(fundCode);
    }

    /**
     * 根据规则代码获取告警记录
     *
     * @param ruleCode 规则代码
     * @return 告警记录列表
     */
    public List<AlertRecord> getAlertRecordsByRuleCode(String ruleCode) {
        return alertRecordMapper.selectByRuleCode(ruleCode);
    }
}