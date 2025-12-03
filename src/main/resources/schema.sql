CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    age INT,
    create_time DATETIME
);

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