package com.sunlight.invest.fund.monitor.mapper;

import com.sunlight.invest.fund.monitor.entity.FundNav;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 基金净值Mapper接口
 *
 * @author System
 * @since 2024-12-02
 */
@Mapper
public interface FundNavMapper {

    /**
     * 插入基金净值记录
     *
     * @param fundNav 基金净值对象
     * @return 影响行数
     */
    @Insert("INSERT INTO fund_nav (fund_code, fund_name, nav_date, unit_nav, daily_return, create_time, update_time) " +
            "VALUES (#{fundCode}, #{fundName}, #{navDate}, #{unitNav}, #{dailyReturn}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FundNav fundNav);

    /**
     * 批量插入基金净值记录（使用ON DUPLICATE KEY UPDATE避免重复）
     *
     * @param fundNavList 基金净值列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO fund_nav (fund_code, fund_name, nav_date, unit_nav, daily_return, create_time, update_time) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.fundCode}, #{item.fundName}, #{item.navDate}, #{item.unitNav}, #{item.dailyReturn}, NOW(), NOW())" +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE " +
            "unit_nav = VALUES(unit_nav), daily_return = VALUES(daily_return), update_time = NOW()" +
            "</script>")
    int batchInsert(@Param("list") List<FundNav> fundNavList);

    /**
     * 根据基金代码和日期查询净值
     *
     * @param fundCode 基金代码
     * @param navDate  净值日期
     * @return 基金净值对象
     */
    @Select("SELECT * FROM fund_nav WHERE fund_code = #{fundCode} AND nav_date = #{navDate}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "navDate", column = "nav_date"),
            @Result(property = "unitNav", column = "unit_nav"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    FundNav selectByCodeAndDate(@Param("fundCode") String fundCode, @Param("navDate") LocalDate navDate);

    /**
     * 查询基金最近N天的净值数据
     *
     * @param fundCode 基金代码
     * @param days     天数
     * @return 基金净值列表（按日期降序）
     */
    @Select("SELECT * FROM fund_nav " +
            "WHERE fund_code = #{fundCode} " +
            "ORDER BY nav_date DESC " +
            "LIMIT #{days}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "navDate", column = "nav_date"),
            @Result(property = "unitNav", column = "unit_nav"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<FundNav> selectRecentDays(@Param("fundCode") String fundCode, @Param("days") int days);

    /**
     * 查询基金指定日期范围的净值数据
     *
     * @param fundCode  基金代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 基金净值列表（按日期升序）
     */
    @Select("SELECT * FROM fund_nav " +
            "WHERE fund_code = #{fundCode} " +
            "AND nav_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY nav_date ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "navDate", column = "nav_date"),
            @Result(property = "unitNav", column = "unit_nav"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<FundNav> selectByDateRange(@Param("fundCode") String fundCode,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * 查询基金最新一条净值记录
     *
     * @param fundCode 基金代码
     * @return 基金净值对象
     */
    @Select("SELECT * FROM fund_nav WHERE fund_code = #{fundCode} ORDER BY nav_date DESC LIMIT 1")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "navDate", column = "nav_date"),
            @Result(property = "unitNav", column = "unit_nav"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    FundNav selectLatest(@Param("fundCode") String fundCode);

    /**
     * 根据基金代码查询指定数量的净值记录
     *
     * @param fundCode 基金代码
     * @param limit    数量限制
     * @return 基金净值列表
     */
    @Select("SELECT * FROM fund_nav WHERE fund_code = #{fundCode} ORDER BY nav_date DESC LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fundCode", column = "fund_code"),
            @Result(property = "fundName", column = "fund_name"),
            @Result(property = "navDate", column = "nav_date"),
            @Result(property = "unitNav", column = "unit_nav"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<FundNav> selectByFundCode(@Param("fundCode") String fundCode, @Param("limit") int limit);

    /**
     * 删除指定日期之前的数据
     *
     * @param fundCode 基金代码
     * @param date     日期
     * @return 影响行数
     */
    @Delete("DELETE FROM fund_nav WHERE fund_code = #{fundCode} AND nav_date < #{date}")
    int deleteBeforeDate(@Param("fundCode") String fundCode, @Param("date") LocalDate date);
}
