package com.sunlight.invest.common;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL执行耗时拦截器测试类
 */
class SqlCostInterceptorTest {

    @Test
    void testFormatSql() {
        SqlCostInterceptor interceptor = new SqlCostInterceptor();
        
        // 测试正常SQL格式化
        String sql1 = "  SELECT * FROM user WHERE id = 1  ";
        String formatted1 = interceptor.formatSql(sql1);
        assertEquals("SELECT * FROM user WHERE id = 1", formatted1);
        
        // 测试包含换行符的SQL格式化
        String sql2 = "  SELECT * \n FROM user \n WHERE id = 1  ";
        String formatted2 = interceptor.formatSql(sql2);
        assertEquals("SELECT * FROM user WHERE id = 1", formatted2);
        
        // 测试包含多个空格的SQL格式化
        String sql3 = "SELECT   *    FROM     user";
        String formatted3 = interceptor.formatSql(sql3);
        assertEquals("SELECT * FROM user", formatted3);
        
        // 测试空SQL
        String sql4 = "";
        String formatted4 = interceptor.formatSql(sql4);
        assertEquals("", formatted4);
        
        // 测试null SQL
        String formatted5 = interceptor.formatSql(null);
        assertEquals("", formatted5);
    }
    
    @Test
    void testFormatParameterValue() {
        SqlCostInterceptor interceptor = new SqlCostInterceptor();
        
        // 测试null值
        String result1 = interceptor.formatParameterValue(null);
        assertEquals("NULL", result1);
        
        // 测试字符串值
        String result2 = interceptor.formatParameterValue("test");
        assertEquals("'test'", result2);
        
        // 测试包含单引号的字符串
        String result3 = interceptor.formatParameterValue("test'value");
        assertEquals("'test''value'", result3);
        
        // 测试数字值
        String result4 = interceptor.formatParameterValue(123);
        assertEquals("123", result4);
        
        // 测试日期值
        Date date = new Date();
        String result5 = interceptor.formatParameterValue(date);
        // 日期格式应该是 'yyyy-MM-dd HH:mm:ss'
        assertTrue(result5.matches("'\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}'"));
    }
}