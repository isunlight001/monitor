package com.sunlight.invest.fund.monitor.mapper;

import com.sunlight.invest.fund.monitor.entity.MonitorFund;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 监控基金Mapper接口
 *
 * @author System
 * @since 2024-12-02
 */
@Mapper
public interface MonitorFundMapper {

    /**
     * 插入监控基金记录
     *
     * @param monitorFund 监控基金对象
     * @return 影响行数
     */
    @Insert("INSERT INTO fund_monitor (fund_code, fund_name, enabled, create_time, update_time) " +
            "VALUES (#{fundCode}, #{fundName}, #{enabled}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MonitorFund monitorFund);

    /**
     * 根据ID查询监控基金
     *
     * @param id 主键ID
     * @return 监控基金对象
     */
    @Select("SELECT * FROM fund_monitor WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    MonitorFund selectById(@Param("id") Long id);

    /**
     * 根据基金代码查询监控基金
     *
     * @param fundCode 基金代码
     * @return 监控基金对象
     */
    @Select("SELECT * FROM fund_monitor WHERE fund_code = #{fundCode}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    MonitorFund selectByFundCode(@Param("fundCode") String fundCode);

    /**
     * 查询所有启用的监控基金
     *
     * @return 监控基金列表
     */
    @Select("SELECT * FROM fund_monitor WHERE enabled = 1 ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<MonitorFund> selectAllEnabled();

    /**
     * 查询所有监控基金
     *
     * @return 监控基金列表
     */
    @Select("SELECT * FROM fund_monitor ORDER BY create_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "enabled", column = "enabled"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<MonitorFund> selectAll();

    /**
     * 更新监控基金
     *
     * @param monitorFund 监控基金对象
     * @return 影响行数
     */
    @Update("UPDATE fund_monitor SET fund_name = #{fundName}, enabled = #{enabled}, update_time = NOW() " +
            "WHERE id = #{id}")
    int update(MonitorFund monitorFund);

    /**
     * 根据ID删除监控基金
     *
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM fund_monitor WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据基金代码删除监控基金
     *
     * @param fundCode 基金代码
     * @return 影响行数
     */
    @Delete("DELETE FROM fund_monitor WHERE fund_code = #{fundCode}")
    int deleteByFundCode(@Param("fundCode") String fundCode);
    
    /**
     * 统计所有监控基金数量
     *
     * @return 监控基金数量
     */
    @Select("SELECT COUNT(*) FROM fund_monitor")
    int countAll();
}