package com.sunlight.invest.common;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
            
            // 填充参数
            String executableSql = getExecutableSql(boundSql, statementHandler);
            
            // 记录SQL执行耗时
            log.info("SQL执行耗时: {} ms, SQL: {}", sqlCost, executableSql);
            
            // 如果执行时间超过1000ms，记录警告日志
            if (sqlCost > 1000) {
                log.warn("SQL执行较慢，耗时: {} ms, SQL: {}", sqlCost, executableSql);
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
    
    /**
     * 获取可执行的SQL语句（包含参数值）
     *
     * @param boundSql BoundSql对象
     * @param statementHandler StatementHandler对象
     * @return 带参数的可执行SQL语句
     */
    private String getExecutableSql(BoundSql boundSql, StatementHandler statementHandler) {
        String sql = boundSql.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        Configuration configuration = getConfiguration(statementHandler);
        if (configuration == null) {
            return formatSql(sql);
        }
        
        // 获取参数对象
        Object parameterObject = boundSql.getParameterObject();
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        
        // 如果没有参数映射，直接返回原SQL
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return formatSql(sql);
        }
        
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        
        // 替换SQL中的参数占位符
        String executableSql = sql;
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                
                // 获取参数值
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                
                // 格式化参数值
                String formattedValue = formatParameterValue(value);
                
                // 替换第一个问号占位符
                executableSql = executableSql.replaceFirst("\\?", formattedValue);
            }
        }
        
        return formatSql(executableSql);
    }
    
    /**
     * 获取Configuration对象
     *
     * @param statementHandler StatementHandler对象
     * @return Configuration对象
     */
    private Configuration getConfiguration(StatementHandler statementHandler) {
        try {
            MetaObject metaObject = MetaObject.forObject(statementHandler, 
                org.apache.ibatis.reflection.SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                org.apache.ibatis.reflection.SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new org.apache.ibatis.reflection.DefaultReflectorFactory());
            return (Configuration) metaObject.getValue("delegate.configuration");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 格式化参数值，使其适合在SQL中显示
     *
     * @param value 参数值
     * @return 格式化后的参数值字符串
     */
    public String formatParameterValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        
        // 处理字符串类型
        if (value instanceof String) {
            return "'" + ((String) value).replace("'", "''") + "'";
        }
        
        // 处理日期类型
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + sdf.format((Date) value) + "'";
        }
        
        // 其他类型直接转换为字符串
        return value.toString();
    }
}