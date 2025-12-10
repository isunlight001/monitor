package com.sunlight.invest.stock.mapper;

import com.sunlight.invest.stock.entity.StockDataEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StockDataMapper {

    /**
     * 创建股票数据表
     */
    @Update("CREATE TABLE IF NOT EXISTS `stock_data` (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "stock_code VARCHAR(20) NOT NULL COMMENT '股票代码'," +
            "stock_name VARCHAR(100) NOT NULL COMMENT '股票名称'," +
            "trade_date DATE NOT NULL COMMENT '交易日期'," +
            "open_price DECIMAL(10,4) COMMENT '开盘价'," +
            "close_price DECIMAL(10,4) COMMENT '收盘价'," +
            "high_price DECIMAL(10,4) COMMENT '最高价'," +
            "low_price DECIMAL(10,4) COMMENT '最低价'," +
            "volume BIGINT COMMENT '成交量'," +
            "amount DECIMAL(15,4) COMMENT '成交额'," +
            "create_time DATE COMMENT '创建时间'," +
            "update_time DATE COMMENT '更新时间'," +
            "UNIQUE KEY uk_stock_date (stock_code, trade_date)" +
            ") COMMENT '股票数据表'")
    void createTable();

    /**
     * 插入股票数据记录
     *
     * @param stockData 股票数据对象
     * @return 影响行数
     */
    @Insert("INSERT INTO stock_data (stock_code, stock_name, trade_date, open_price, close_price, high_price, low_price, volume, amount, create_time, update_time) " +
            "VALUES (#{stockCode}, #{stockName}, #{tradeDate}, #{openPrice}, #{closePrice}, #{highPrice}, #{lowPrice}, #{volume}, #{amount}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StockDataEntity stockData);

    /**
     * 批量插入股票数据记录（使用ON DUPLICATE KEY UPDATE避免重复）
     *
     * @param stockDataList 股票数据列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO stock_data (stock_code, stock_name, trade_date, open_price, close_price, high_price, low_price, volume, amount, create_time, update_time) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.stockCode}, #{item.stockName}, #{item.tradeDate}, #{item.openPrice}, #{item.closePrice}, #{item.highPrice}, #{item.lowPrice}, #{item.volume}, #{item.amount}, #{item.createTime}, #{item.updateTime})" +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE " +
            "open_price = VALUES(open_price), " +
            "close_price = VALUES(close_price), " +
            "high_price = VALUES(high_price), " +
            "low_price = VALUES(low_price), " +
            "volume = VALUES(volume), " +
            "amount = VALUES(amount), " +
            "update_time = VALUES(update_time)" +
            "</script>")
    int batchInsert(@Param("list") List<StockDataEntity> stockDataList);

    /**
     * 根据股票代码和交易日期查询数据
     *
     * @param stockCode 股票代码
     * @param tradeDate 交易日期
     * @return 股票数据对象
     */
    @Select("SELECT * FROM stock_data WHERE stock_code = #{stockCode} AND trade_date = #{tradeDate}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "stockCode", column = "stock_code"),
            @Result(property = "stockName", column = "stock_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    StockDataEntity selectByCodeAndDate(@Param("stockCode") String stockCode, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询股票最近N天的数据
     *
     * @param stockCode 股票代码
     * @param days      天数
     * @return 股票数据列表（按日期降序）
     */
    @Select("SELECT * FROM stock_data " +
            "WHERE stock_code = #{stockCode} " +
            "ORDER BY trade_date DESC " +
            "LIMIT #{days}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "stockCode", column = "stock_code"),
            @Result(property = "stockName", column = "stock_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<StockDataEntity> selectRecentData(@Param("stockCode") String stockCode, @Param("days") int days);

    /**
     * 查询指定日期范围内的股票数据
     *
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 股票数据列表（按日期升序）
     */
    @Select("SELECT * FROM stock_data " +
            "WHERE stock_code = #{stockCode} AND trade_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY trade_date ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "stockCode", column = "stock_code"),
            @Result(property = "stockName", column = "stock_name"),
            @Result(property = "tradeDate", column = "trade_date"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<StockDataEntity> selectByDateRange(@Param("stockCode") String stockCode, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}