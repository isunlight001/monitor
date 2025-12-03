package com.sunlight.invest.fund.monitor.service;

import com.sunlight.invest.fund.monitor.entity.AlarmRecord;
import com.sunlight.invest.fund.monitor.mapper.AlarmRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 告警记录服务类
 *
 * @author System
 * @since 2024-12-03
 */
@Service
public class AlarmRecordService {

    @Autowired
    private AlarmRecordMapper alarmRecordMapper;

    /**
     * 保存告警记录
     *
     * @param alarmRecord 告警记录实体
     * @return 保存的记录数
     */
    public int saveAlarmRecord(AlarmRecord alarmRecord) {
        return alarmRecordMapper.insert(alarmRecord);
    }
}