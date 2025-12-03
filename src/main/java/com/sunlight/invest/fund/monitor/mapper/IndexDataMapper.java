package com.sunlight.invest.fund.monitor.mapper;

import com.sunlight.invest.fund.monitor.entity.IndexData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 指数数据Mapper接口
 *
 * @author System
 * @since 2024-12-03
 */
@Mapper
public interface IndexDataMapper {

    /**
     * 创建指数数据表
     */
    @Update("CREATE TABLE IF NOT EXISTS `index_data` (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "index_code VARCHAR(20) NOT NULL COMMENT '指数代码', " +
            "index_name VARCHAR(100) NOT NULL COMMENT '指数名称', " +
            "trade_date DATE NOT NULL COMMENT '交易日期', " +
            "open_price DECIMAL(10,4) COMMENT '开盘价', " +
            "close_price DECIMAL(10,4) COMMENT '收盘价', " +
            "high_price DECIMAL(10,4) COMMENT '最高价', " +
            "low_price DECIMAL(10,4) COMMENT '最低价', " +
            "daily_return DECIMAL(10,4) COMMENT '日涨跌幅', " +
            "volume BIGINT COMMENT '成交量', " +
            "amount DECIMAL(15,4) COMMENT '成交额', " +
            "create_time DATETIME COMMENT '创建时间', " +
            "update_time DATETIME COMMENT '更新时间', " +
            "UNIQUE KEY uk_index_date (index_code, trade_date)" +
            ") COMMENT '指数数据表'")
    void createTable();

    /**
     * 插入指数数据记录
     *
     * @param indexData 指数数据对象
     * @return 影响行数
     */
    @Insert("INSERT INTO index_data (index_code, index_name, trade_date, open_price, close_price, high_price, low_price, daily_return, volume, amount, create_time, update_time) " +
            "VALUES (#{indexCode}, #{indexName}, #{tradeDate}, #{openPrice}, #{closePrice}, #{highPrice}, #{lowPrice}, #{dailyReturn}, #{volume}, #{amount}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(IndexData indexData);

    /**
     * 批量插入指数数据记录（使用ON DUPLICATE KEY UPDATE避免重复）
     *
     * @param indexDataList 指数数据列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO index_data (index_code, index_name, trade_date, open_price, close_price, high_price, low_price, daily_return, volume, amount, create_time, update_time) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.indexCode}, #{item.indexName}, #{item.tradeDate}, #{item.openPrice}, #{item.closePrice}, #{item.highPrice}, #{item.lowPrice}, #{item.dailyReturn}, #{item.volume}, #{item.amount}, NOW(), NOW())" +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE " +
            "open_price = VALUES(open_price), close_price = VALUES(close_price), high_price = VALUES(high_price), low_price = VALUES(low_price), " +
            "daily_return = VALUES(daily_return), volume = VALUES(volume), amount = VALUES(amount), update_time = NOW()" +
            "</script>")
    int batchInsert(@Param("list") List<IndexData> indexDataList);

    /**
     * 根据指数代码和日期查询数据
     *
     * @param indexCode 指数代码
     * @param tradeDate 交易日期
     * @return 指数数据对象
     */
    @Select("SELECT * FROM index_data WHERE index_code = #{indexCode} AND trade_date = #{tradeDate}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "indexCode", column = "index_code"),
            @Result(property = "indexName", column = "index_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    IndexData selectByCodeAndDate(@Param("indexCode") String indexCode, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询指数最近N天的数据
     *
     * @param indexCode 指数代码
     * @param days      天数
     * @return 指数数据列表（按日期降序）
     */
    @Select("SELECT * FROM index_data " +
            "WHERE index_code = #{indexCode} " +
            "ORDER BY trade_date DESC " +
            "LIMIT #{days}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "indexCode", column = "index_code"),
            @Result(property = "indexName", column = "index_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<IndexData> selectRecentDays(@Param("indexCode") String indexCode, @Param("days") int days);

    /**
     * 查询指数指定日期范围的数据
     *
     * @param indexCode  指数代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 指数数据列表（按日期升序）
     */
    @Select("SELECT * FROM index_data " +
            "WHERE index_code = #{indexCode} " +
            "AND trade_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY trade_date ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "indexCode", column = "index_code"),
            @Result(property = "indexName", column = "index_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<IndexData> selectByDateRange(@Param("indexCode") String indexCode,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 查询指数最新一条记录
     *
     * @param indexCode 指数代码
     * @return 指数数据对象
     */
    @Select("SELECT * FROM index_data WHERE index_code = #{indexCode} ORDER BY trade_date DESC LIMIT 1")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "indexCode", column = "index_code"),
            @Result(property = "indexName", column = "index_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "dailyReturn", column = "daily_return"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    IndexData selectLatest(@Param("indexCode") String indexCode);

    /**
     * 删除指定日期之前的数据
     *
     * @param indexCode 指数代码
     * @param date      日期
     * @return 影响行数
     */
    @Delete("DELETE FROM index_data WHERE index_code = #{indexCode} AND trade_date < #{date}")
    int deleteBeforeDate(@Param("indexCode") String indexCode, @Param("date") LocalDate date);
}