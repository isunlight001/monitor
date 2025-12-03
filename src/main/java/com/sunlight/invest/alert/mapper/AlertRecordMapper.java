package com.sunlight.invest.alert.mapper;

import com.sunlight.invest.alert.entity.AlertRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 告警记录Mapper接口
 *
 * @author System
 * @since 2024-12-03
 */
@Mapper
public interface AlertRecordMapper {

    /**
     * 插入告警记录
     *
     * @param alertRecord 告警记录实体
     * @return 插入记录数
     */
    @Insert({
        "<script>",
        "INSERT INTO alert_record (",
        "fund_code, fund_name, index_code, index_name, alert_type,",
        "rule_code, rule_description, consecutive_days, cumulative_return,",
        "daily_return, nav_date, unit_nav, volume, amount, alert_content",
        ") VALUES (",
        "#{fundCode}, #{fundName}, #{indexCode}, #{indexName}, #{alertType},",
        "#{ruleCode}, #{ruleDescription}, #{consecutiveDays}, #{cumulativeReturn},",
        "#{dailyReturn}, #{navDate}, #{unitNav}, #{volume}, #{amount}, #{alertContent}",
        ")",
        "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AlertRecord alertRecord);

    /**
     * 查询所有告警记录，按创建时间倒序排列
     *
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alert_record ORDER BY create_time DESC LIMIT 100")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fundCode", column = "fund_code"),
        @Result(property = "fundName", column = "fund_name"),
        @Result(property = "indexCode", column = "index_code"),
        @Result(property = "indexName", column = "index_name"),
        @Result(property = "alertType", column = "alert_type"),
        @Result(property = "ruleCode", column = "rule_code"),
        @Result(property = "ruleDescription", column = "rule_description"),
        @Result(property = "consecutiveDays", column = "consecutive_days"),
        @Result(property = "cumulativeReturn", column = "cumulative_return"),
        @Result(property = "dailyReturn", column = "daily_return"),
        @Result(property = "navDate", column = "nav_date"),
        @Result(property = "unitNav", column = "unit_nav"),
        @Result(property = "volume", column = "volume"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "alertContent", column = "alert_content"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<AlertRecord> selectAll();

    /**
     * 根据ID查询告警记录
     *
     * @param id 告警记录ID
     * @return 告警记录实体
     */
    @Select("SELECT * FROM alert_record WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fundCode", column = "fund_code"),
        @Result(property = "fundName", column = "fund_name"),
        @Result(property = "indexCode", column = "index_code"),
        @Result(property = "indexName", column = "index_name"),
        @Result(property = "alertType", column = "alert_type"),
        @Result(property = "ruleCode", column = "rule_code"),
        @Result(property = "ruleDescription", column = "rule_description"),
        @Result(property = "consecutiveDays", column = "consecutive_days"),
        @Result(property = "cumulativeReturn", column = "cumulative_return"),
        @Result(property = "dailyReturn", column = "daily_return"),
        @Result(property = "navDate", column = "nav_date"),
        @Result(property = "unitNav", column = "unit_nav"),
        @Result(property = "volume", column = "volume"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "alertContent", column = "alert_content"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    AlertRecord selectById(Long id);

    /**
     * 根据基金代码查询告警记录
     *
     * @param fundCode 基金代码
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alert_record WHERE fund_code = #{fundCode} ORDER BY create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fundCode", column = "fund_code"),
        @Result(property = "fundName", column = "fund_name"),
        @Result(property = "indexCode", column = "index_code"),
        @Result(property = "indexName", column = "index_name"),
        @Result(property = "alertType", column = "alert_type"),
        @Result(property = "ruleCode", column = "rule_code"),
        @Result(property = "ruleDescription", column = "rule_description"),
        @Result(property = "consecutiveDays", column = "consecutive_days"),
        @Result(property = "cumulativeReturn", column = "cumulative_return"),
        @Result(property = "dailyReturn", column = "daily_return"),
        @Result(property = "navDate", column = "nav_date"),
        @Result(property = "unitNav", column = "unit_nav"),
        @Result(property = "volume", column = "volume"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "alertContent", column = "alert_content"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<AlertRecord> selectByFundCode(String fundCode);

    /**
     * 根据规则代码查询告警记录
     *
     * @param ruleCode 规则代码
     * @return 告警记录列表
     */
    @Select("SELECT * FROM alert_record WHERE rule_code = #{ruleCode} ORDER BY create_time DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "fundCode", column = "fund_code"),
        @Result(property = "fundName", column = "fund_name"),
        @Result(property = "indexCode", column = "index_code"),
        @Result(property = "indexName", column = "index_name"),
        @Result(property = "alertType", column = "alert_type"),
        @Result(property = "ruleCode", column = "rule_code"),
        @Result(property = "ruleDescription", column = "rule_description"),
        @Result(property = "consecutiveDays", column = "consecutive_days"),
        @Result(property = "cumulativeReturn", column = "cumulative_return"),
        @Result(property = "dailyReturn", column = "daily_return"),
        @Result(property = "navDate", column = "nav_date"),
        @Result(property = "unitNav", column = "unit_nav"),
        @Result(property = "volume", column = "volume"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "alertContent", column = "alert_content"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<AlertRecord> selectByRuleCode(String ruleCode);
}