package com.sunlight.invest.fund.monitor.mapper;

import com.sunlight.invest.fund.monitor.entity.AlarmRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 告警记录Mapper接口
 *
 * @author System
 * @since 2024-12-03
 */
@Mapper
public interface AlarmRecordMapper {

    /**
     * 插入告警记录
     *
     * @param alarmRecord 告警记录实体
     * @return 插入记录数
     */
    @Insert("INSERT INTO alarm_record (fund_code, fund_name, rule_code, rule_description, " +
            "consecutive_days, cumulative_return, daily_return, nav_date, unit_nav, alarm_content) " +
            "VALUES (#{fundCode}, #{fundName}, #{ruleCode}, #{ruleDescription}, " +
            "#{consecutiveDays}, #{cumulativeReturn}, #{dailyReturn}, #{navDate}, #{unitNav}, #{alarmContent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AlarmRecord alarmRecord);
}