package com.sunlight.invest.common;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.util.Properties;

/**
 * SQL执行耗时拦截器
 * <p>
 * 用于监控SQL执行耗时，帮助优化数据库查询性能
 * </p>
 *
 * @author System
 * @since 2024-12-03
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
})
public class SqlCostInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SqlCostInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            // 执行SQL
            result = invocation.proceed();
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long sqlCost = endTime - startTime;
            
            // 获取SQL语句
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            
            // 格式化SQL（去除多余空格和换行）
            sql = formatSql(sql);
            
            // 记录SQL执行耗时
            log.info("SQL执行耗时: {} ms, SQL: {}", sqlCost, sql);
            
            // 如果执行时间超过1000ms，记录警告日志
            if (sqlCost > 1000) {
                log.warn("SQL执行较慢，耗时: {} ms, SQL: {}", sqlCost, sql);
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以设置一些自定义属性
    }

    /**
     * 格式化SQL语句，去除多余空格和换行
     *
     * @param sql 原始SQL语句
     * @return 格式化后的SQL语句
     */
    public String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        // 去除首尾空格
        sql = sql.trim();
        
        // 将多个连续的空格替换为单个空格
        sql = sql.replaceAll("\\s+", " ");
        
        // 限制SQL长度，避免日志过长
        if (sql.length() > 1000) {
            sql = sql.substring(0, 1000) + "...";
        }
        
        return sql;
    }
}