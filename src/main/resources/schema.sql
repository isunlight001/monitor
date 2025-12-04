-- 基金净值表
CREATE TABLE IF NOT EXISTS `fund_nav` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    fund_name VARCHAR(100) NOT NULL COMMENT '基金名称',
    nav_date DATE NOT NULL COMMENT '净值日期',
    unit_nav DECIMAL(10,4) NOT NULL COMMENT '单位净值',
    daily_return DECIMAL(10,4) COMMENT '日涨跌幅',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_fund_date (fund_code, nav_date)
) COMMENT '基金净值表';

-- 基金监控表
CREATE TABLE IF NOT EXISTS `fund_monitor` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    fund_name VARCHAR(100) NOT NULL COMMENT '基金名称',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用监控 (1:启用, 0:禁用)',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_fund_code (fund_code)
) COMMENT '基金监控表';

-- 指数数据表
CREATE TABLE IF NOT EXISTS `index_data` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    index_code VARCHAR(20) NOT NULL COMMENT '指数代码',
    index_name VARCHAR(100) NOT NULL COMMENT '指数名称',
    trade_date DATE NOT NULL COMMENT '交易日期',
    open_price DECIMAL(10,4) COMMENT '开盘价',
    close_price DECIMAL(10,4) COMMENT '收盘价',
    high_price DECIMAL(10,4) COMMENT '最高价',
    low_price DECIMAL(10,4) COMMENT '最低价',
    daily_return DECIMAL(10,4) COMMENT '日涨跌幅',
    volume BIGINT COMMENT '成交量',
    amount DECIMAL(15,4) COMMENT '成交额',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_index_date (index_code, trade_date)
) COMMENT '指数数据表';

-- 告警记录表
CREATE TABLE IF NOT EXISTS `alarm_record` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) COMMENT '基金代码',
    fund_name VARCHAR(100) COMMENT '基金名称',
    rule_code VARCHAR(20) NOT NULL COMMENT '规则代码 (A,B,C,D,E等)',
    rule_description VARCHAR(200) NOT NULL COMMENT '规则描述',
    consecutive_days INT COMMENT '连续天数',
    cumulative_return DECIMAL(10,4) COMMENT '累计涨跌幅',
    daily_return DECIMAL(10,4) COMMENT '单日涨跌幅',
    nav_date DATE COMMENT '净值日期',
    unit_nav DECIMAL(10,4) COMMENT '单位净值',
    alarm_content TEXT COMMENT '告警内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_fund_code (fund_code),
    INDEX idx_rule_code (rule_code),
    INDEX idx_create_time (create_time)
) COMMENT '告警记录表';

-- 邮件接收人表
CREATE TABLE IF NOT EXISTS `email_recipient` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '接收人姓名',
    email VARCHAR(100) NOT NULL COMMENT '接收人邮箱地址',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_email (email)
) COMMENT '邮件接收人表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置描述',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '系统配置表';
