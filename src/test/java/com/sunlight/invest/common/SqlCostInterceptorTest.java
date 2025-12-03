package com.sunlight.invest.common;

import org.junit.jupiter.api.Test;
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
}