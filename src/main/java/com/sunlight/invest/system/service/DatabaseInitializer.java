package com.sunlight.invest.system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库初始化服务
 * 在应用启动时初始化数据库表结构和初始数据
 *
 * @author System
 * @since 2024-12-06
 */
@Service
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("classpath:schema.sql")
    private Resource schemaResource;

    @Value("classpath:init.sql")
    private Resource initResource;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;

    /**
     * 应用启动时初始化数据库
     */
    @PostConstruct
    public void initializeDatabase() {
        try {
            log.info("开始初始化数据库...");

            // 检查并创建数据库（如果是MySQL）
            if (dbUrl.contains("mysql")) {
                createDatabaseIfNotExists();
            }

            // 执行表结构初始化
            if (schemaResource.exists()) {
                executeSqlScript(schemaResource, "schema.sql");
            } else {
                log.info("未找到schema.sql文件，跳过表结构初始化");
            }

            // 执行初始数据初始化
            if (initResource.exists()) {
                executeSqlScript(initResource, "init.sql");
            } else {
                log.info("未找到init.sql文件，跳过初始数据初始化");
            }

            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }

    /**
     * 检查并创建数据库（仅适用于MySQL）
     */
    private void createDatabaseIfNotExists() {
        try {
            // 解析数据库名称
            String dbName = extractDatabaseName(dbUrl);
            if (dbName == null) {
                log.warn("无法从URL中提取数据库名称: {}", dbUrl);
                return;
            }

            // 创建不带数据库名的连接URL
            String baseDbUrl = dbUrl.substring(0, dbUrl.indexOf(dbName));
            
            log.info("检查数据库 {} 是否存在...", dbName);
            
            // 使用不带数据库名的URL连接
            try (Connection connection = DriverManager.getConnection(baseDbUrl, dbUsername, dbPassword)) {
                // 检查数据库是否存在
                if (!databaseExists(connection, dbName)) {
                    log.info("数据库 {} 不存在，正在创建...", dbName);
                    try (Statement stmt = connection.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                        log.info("数据库 {} 创建成功", dbName);
                    }
                } else {
                    log.info("数据库 {} 已存在", dbName);
                }
            }
        } catch (Exception e) {
            log.warn("检查或创建数据库时出错: {}", e.getMessage());
            // 不中断应用程序启动
        }
    }

    /**
     * 从数据库URL中提取数据库名称
     */
    private String extractDatabaseName(String url) {
        try {
            // 匹配jdbc:mysql://host:port/databaseName格式
            Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("解析数据库名称时出错: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查数据库是否存在
     */
    private boolean databaseExists(Connection connection, String dbName) {
        try (ResultSet rs = connection.getMetaData().getCatalogs()) {
            while (rs.next()) {
                if (dbName.equals(rs.getString("TABLE_CAT"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("检查数据库是否存在时出错: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 执行SQL脚本文件
     *
     * @param resource SQL脚本资源
     * @param fileName 文件名（用于日志）
     */
    private void executeSqlScript(Resource resource, String fileName) {
        try {
            log.info("开始执行SQL脚本: {}", fileName);

            // 读取SQL脚本内容
            StringBuilder sqlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlBuilder.append(line).append("\n");
                }
            }

            String sqlScript = sqlBuilder.toString();
            
            // 分割SQL语句（以分号分割）
            List<String> sqlStatements = splitSqlStatements(sqlScript);
            
            // 执行每个SQL语句
            for (String sql : sqlStatements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--") && !sql.startsWith("/*")) {
                    try {
                        // 检查是否是CREATE TABLE语句
                        if (isCreateTableStatement(sql)) {
                            String tableName = extractTableName(sql);
                            if (tableName != null && tableExists(tableName)) {
                                log.info("表 {} 已存在，跳过创建", tableName);
                                continue;
                            }
                        }
                        
                        // 检查是否是INSERT语句
                        if (isInsertStatement(sql)) {
                            if (shouldSkipInsert(sql)) {
                                log.info("数据已存在，跳过插入: {}", sql.substring(0, Math.min(sql.length(), 50)) + (sql.length() > 50 ? "..." : ""));
                                continue;
                            }
                        }
                        
                        // 执行SQL语句
                        jdbcTemplate.execute(sql);
                        log.debug("执行SQL成功: {}", sql.substring(0, Math.min(sql.length(), 100)) + (sql.length() > 100 ? "..." : ""));
                    } catch (Exception e) {
                        log.warn("执行SQL语句失败，已跳过: {}", sql.substring(0, Math.min(sql.length(), 100)) + (sql.length() > 100 ? "..." : ""));
                    }
                }
            }

            log.info("SQL脚本 {} 执行完成", fileName);
        } catch (Exception e) {
            log.error("执行SQL脚本 {} 失败", fileName, e);
        }
    }

    /**
     * 分割SQL语句
     *
     * @param sqlScript SQL脚本内容
     * @return SQL语句列表
     */
    private List<String> splitSqlStatements(String sqlScript) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        
        String[] lines = sqlScript.split("\n");
        for (String line : lines) {
            line = line.trim();
            
            // 忽略空行和注释行
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            
            // 处理多行注释
            if (line.startsWith("/*")) {
                continue;
            }
            
            currentStatement.append(line).append(" ");
            
            // 如果行以分号结尾，则是一个完整的SQL语句
            if (line.endsWith(";")) {
                statements.add(currentStatement.toString().trim());
                currentStatement = new StringBuilder();
            }
        }
        
        // 添加最后一个可能没有分号的语句
        String lastStatement = currentStatement.toString().trim();
        if (!lastStatement.isEmpty()) {
            statements.add(lastStatement);
        }
        
        return statements;
    }

    /**
     * 检查是否是CREATE TABLE语句
     */
    private boolean isCreateTableStatement(String sql) {
        return sql.toUpperCase().startsWith("CREATE TABLE");
    }

    /**
     * 从CREATE TABLE语句中提取表名
     */
    private String extractTableName(String sql) {
        // 提取表名的正则表达式
        Pattern pattern = Pattern.compile("CREATE\\s+TABLE\\s+(IF\\s+NOT\\s+EXISTS\\s+)?[`]?([a-zA-Z0-9_]+)[`]?", 
                                         Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql.trim());
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"});
            return tables.next();
        } catch (Exception e) {
            log.warn("检查表 {} 是否存在时出错: {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否是INSERT语句
     */
    private boolean isInsertStatement(String sql) {
        return sql.toUpperCase().startsWith("INSERT");
    }

    /**
     * 检查是否应该跳过INSERT语句
     * 通过检查唯一约束来避免重复插入
     */
    private boolean shouldSkipInsert(String sql) {
        try {
            // 解析INSERT语句，检查是否违反唯一约束
            // 这里采用简化的策略：尝试执行，如果违反唯一约束则忽略错误
            // 更复杂的实现可以预先查询数据是否存在
            
            // 对于一些关键表，我们可以实现特定的检查逻辑
            if (sql.contains("system_config") && sql.contains("uk_config_key")) {
                // 检查system_config表的config_key是否已存在
                return checkSystemConfigExists(sql);
            } else if (sql.contains("email_recipient") && sql.contains("uk_email")) {
                // 检查email_recipient表的email是否已存在
                return checkEmailRecipientExists(sql);
            }
            
            // 默认情况下不跳过
            return false;
        } catch (Exception e) {
            log.warn("检查INSERT语句时出错: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查system_config表的config_key是否已存在
     */
    private boolean checkSystemConfigExists(String sql) {
        try {
            // 从INSERT语句中提取config_key值
            Pattern pattern = Pattern.compile("'([a-zA-Z0-9_]+)'", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sql);
            if (matcher.find()) {
                String configKey = matcher.group(1);
                String checkSql = "SELECT COUNT(*) FROM system_config WHERE config_key = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, configKey);
                return count != null && count > 0;
            }
        } catch (Exception e) {
            log.warn("检查system_config数据是否存在时出错: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查email_recipient表的email是否已存在
     */
    private boolean checkEmailRecipientExists(String sql) {
        try {
            // 从INSERT语句中提取email值
            Pattern pattern = Pattern.compile("'([^']+@[^']+)'", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sql);
            if (matcher.find()) {
                String email = matcher.group(1);
                String checkSql = "SELECT COUNT(*) FROM email_recipient WHERE email = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, email);
                return count != null && count > 0;
            }
        } catch (Exception e) {
            log.warn("检查email_recipient数据是否存在时出错: {}", e.getMessage());
        }
        return false;
    }
}