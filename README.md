# 基金监控系统

## 📋 项目概述

本项目是一个综合性的监控系统，包含两个主要功能模块：

1. **基金监控系统** - 自动抓取基金净值数据并监控异常波动
2. **AI智能助手** - 集成DeepSeek大模型，提供智能问答功能

## 🏗️ 技术栈

- **后端框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0.33
- **连接池**: Druid
- **ORM框架**: MyBatis
- **邮件服务**: Spring Mail
- **HTTP客户端**: OkHttp3, Jsoup
- **Excel处理**: Apache POI
- **监控**: Spring Boot Actuator, Micrometer Prometheus
- **AI服务**: DeepSeek大模型
- **构建工具**: Maven
- **Java版本**: Java 8

## 📁 项目结构

```
src/main/java/com/sunlight/invest/
├── FundApplication.java              # 应用启动类
├── fund/
│   ├── Fund.java                     # 基金实体类
│   ├── FundCrawler.java              # 基金数据爬虫
│   ├── FundExcelExporter.java        # 基金数据导出Excel
│   ├── FundHistoryToExcel.java       # 基金历史数据导出主程序
│   ├── ShanghaiIndexDownloader.java   # 上证指数数据下载
│   ├── ShanghaiIndexTushareDownloader.java # Tushare上证指数下载
│   ├── export/
│   │   └── GsNavHtmlToExcel.java     # 基金净值HTML转Excel
│   ├── backtest/
│   │   ├── FundBacktest.java         # 基金回测主程序
│   │   ├── controller/
│   │   │   └── FundBacktestController.java # 回测控制器
│   │   ├── dto/
│   │   │   ├── BacktestRequest.java  # 回测请求DTO
│   │   │   └── BacktestResponse.java # 回测响应DTO
│   │   └── service/
│   │       └── FundBacktestService.java # 回测服务
│   └── monitor/
│       ├── entity/
│       │   ├── FundNav.java          # 基金净值实体类
│       │   └── MonitorFund.java      # 监控基金实体类（包含备注字段）
│       ├── mapper/
│       │   ├── FundNavMapper.java    # 基金净值Mapper
│       │   └── MonitorFundMapper.java # 监控基金Mapper
│       ├── service/
│       │   ├── FundCrawlerService.java # 基金数据爬取服务
│       │   └── FundMonitorService.java # 基金监控服务
│       ├── schedule/
│       │   └── FundMonitorScheduler.java # 定时任务调度器
│       └── controller/
│           └── FundMonitorController.java # 基金监控控制器
├── notification/
│   ├── config/
│   │   └── NotificationProperties.java # 通知配置属性
│   ├── dto/
│   │   ├── NotificationRequest.java   # 通知请求DTO
│   │   └── NotificationResponse.java  # 通知响应DTO
│   └── service/
│       ├── EmailNotificationService.java # 邮件通知服务
│       ├── WeChatNotificationService.java # 微信通知服务
│       └── NotificationService.java   # 统一通知服务
├── threadpool/
│   ├── config/
│   │   └── ThreadPoolConfig.java     # 线程池配置
│   ├── controller/
│   │   └── ThreadPoolController.java # 线程池控制器
│   └── service/
│       └── ThreadPoolService.java    # 线程池服务
└── service/
    └── UserService.java              # 用户服务

src/main/java/com/sunlight/ai/
├── config/
│   └── DeepSeekConfig.java          # DeepSeek配置类
├── controller/
│   └── AIController.java            # AI控制器
├── service/
│   └── DeepSeekService.java         # DeepSeek服务类
└── test/
    └── AIServiceTest.java           # AI服务测试类

src/main/resources/
├── static/
│   ├── fund-backtest.html            # 基金回测页面
│   ├── fund-monitor.html             # 基金监控页面
│   ├── notification-test.html        # 通知测试页面
│   ├── ai-test.html                  # AI测试页面
│   └── index.html                    # 首页
├── application.yml                   # 应用配置文件
├── application-secrets.yml          # 敏感信息配置文件（需自行创建）
├── application-secrets-example.yml  # 敏感信息配置文件示例
└── schema.sql                       # 数据库表结构
```

## 🚀 功能模块

### 1. 基金监控系统

#### 核心功能
- **数据获取**: 每晚11点自动抓取基金净值数据
- **增量更新**: 支持最近1个月数据的增量更新
- **监控规则**:
  - 规则A: 连续4天或以上上涨/下跌
  - 规则B: 单日涨跌幅绝对值≥5%
  - 规则C: 连续2-3天累计涨跌幅绝对值≥5%
- **邮件预警**: 异常波动时自动发送邮件通知
- **数据存储**: MySQL数据库持久化存储
- **监控管理**: 支持通过Web界面动态添加、删除和管理监控基金列表
- **基金备注**: 支持为每个监控基金添加备注信息，方便分类和管理
- **编辑功能**: 支持在线编辑基金备注信息
- **容器化部署**: 支持Docker和docker-compose一键部署

#### API接口
- `POST /api/fund/monitor/crawl` - 抓取基金数据
- `POST /api/fund/monitor/update` - 增量更新基金数据
- `POST /api/fund/monitor/check` - 执行监控检查
- `GET /api/fund/monitor/nav` - 查询基金净值
- `POST /api/fund/monitor/monitor-fund` - 添加监控基金（支持备注字段）
- `GET /api/fund/monitor/monitor-funds` - 查询所有监控基金
- `PUT /api/fund/monitor/monitor-fund/{id}/status` - 更新监控基金状态
- `PUT /api/fund/monitor/monitor-fund/{id}` - 更新监控基金信息（包括备注）
- `DELETE /api/fund/monitor/monitor-fund/{id}` - 删除监控基金

#### 定时任务
- 每晚11点自动执行数据抓取和监控检查

### 2. 线程池监控系统

#### 核心功能
- **线程池监控**: 实时监控应用中的线程池状态
- **性能观测**: 监控线程池的活跃线程数、队列大小等指标
- **Actuator集成**: 通过Spring Boot Actuator暴露监控端点
- **Prometheus集成**: 支持Prometheus监控数据导出

#### API接口
- `GET /api/threadpool/metrics` - 获取线程池指标
- `GET /api/threadpool/status` - 获取线程池状态

### 3. 通知系统

#### 核心功能
- **邮件通知**: 支持文本和HTML格式邮件
- **微信通知**: 支持Server酱和企业微信
- **统一接口**: 提供统一的通知发送接口
- **配置管理**: 灵活的配置管理机制
- **邮件接收人管理**: 支持邮件接收人的增删改查操作

#### API接口
- `POST /api/notification/send` - 发送通知
- `GET /api/notification/status` - 检查服务状态
- `POST /api/notification/test/email` - 测试邮件发送
- `POST /api/notification/test/wechat` - 测试微信发送
- `POST /api/email-recipients` - 添加邮件接收人
- `GET /api/email-recipients/{id}` - 根据ID查询邮件接收人
- `GET /api/email-recipients` - 查询所有邮件接收人
- `GET /api/email-recipients/enabled` - 查询所有启用的邮件接收人
- `PUT /api/email-recipients` - 更新邮件接收人
- `DELETE /api/email-recipients/{id}` - 删除邮件接收人

### 4. AI智能助手

#### 核心功能
- **智能问答**: 集成DeepSeek大模型，提供自然语言问答能力
- **API接口**: 提供RESTful API接口供其他模块调用
- **Web界面**: 提供友好的Web聊天界面进行测试
- **配置管理**: 支持通过配置文件管理API密钥等参数

#### API接口
- `POST /api/ai/chat` - 发送问题并获取AI回答（表单参数）
- `POST /api/ai/chat/json` - 发送问题并获取AI回答（JSON参数）

#### Web界面
- **AI测试页面**: http://localhost:8080/ai-test.html

## ⚙️ 配置说明

### 环境配置
为了保护敏感信息，本项目将敏感配置独立出来。请按以下步骤配置：

1. 复制 `src/main/resources/application-secrets-example.yml` 文件
2. 将复制的文件重命名为 `application-secrets.yml`
3. 在 `application-secrets.yml` 中填写实际的敏感信息

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fund?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
    username: your_database_username
    password: your_database_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 邮件配置
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@example.com
    password: your_email_password
```

### DeepSeek AI配置
```yaml
deepseek:
  api-key: your_deepseek_api_key
  api-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
```

### 基金监控配置
```yaml
fund:
  monitor:
    codes: 006195:国金量化多因子,002170:东吴移动互联
```

### 邮件通知配置
```yaml
notification:
  mail:
    enabled: true
    pass: your-email-password
    from: your-email@qq.com
    to: receiver-email@qq.com
```

### 邮件接收人管理
系统支持通过Web界面或API接口管理邮件接收人列表，可以添加、编辑、删除和查询邮件接收人信息。
所有启用的邮件接收人都会在基金预警时收到通知邮件。

访问地址: http://localhost:8080/email-recipient-management.html

## 🛠️ 构建与运行

### 环境要求
- Java 8
- MySQL 8.0.33
- Maven 3.6+
- Docker (可选，用于容器化部署)

### 构建项目
```bash
mvn clean compile
```

### 运行应用
```bash
mvn spring-boot:run
```

### 打包部署
```bash
mvn clean package
java -jar target/monitor-1.0-SNAPSHOT.jar
```

### Docker容器化部署
```bash
# 使用docker-compose一键部署
docker-compose up -d

# 单独构建Docker镜像
docker build -t fund-monitor .

# 运行容器
docker run -d -p 8080:8080 fund-monitor
```

## 🌐 访问地址

启动应用后，可通过以下URL访问：

- **首页**: http://localhost:8080/
- **基金回测**: http://localhost:8080/fund-backtest.html
- **基金监控**: http://localhost:8080/fund-monitor.html
- **通知测试**: http://localhost:8080/notification-test.html
- **邮件接收人管理**: http://localhost:8080/email-recipient-management.html
- **AI测试**: http://localhost:8080/ai-test.html

## 🧪 测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试
```bash
mvn test -Dtest=FundCrawlerServiceTest
mvn test -Dtest=EmailNotificationServiceTest
```

## 📊 数据库表结构

### 用户表 (user)
```sql
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    age INT,
    create_time DATETIME
);
```

### 基金净值表 (fund_nav)
```sql
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
```

### 基金监控表 (fund_monitor)
```sql
CREATE TABLE IF NOT EXISTS `fund_monitor` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL COMMENT '基金代码',
    fund_name VARCHAR(100) NOT NULL COMMENT '基金名称',
    remark VARCHAR(200) COMMENT '基金备注',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用监控 (1:启用, 0:禁用)',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_fund_code (fund_code)
) COMMENT '基金监控表';

-- 为基金监控表添加备注字段
ALTER TABLE `fund_monitor` ADD COLUMN remark VARCHAR(200) COMMENT '基金备注';
```
```

### 告警记录表 (alarm_record)
```sql
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
```

### 邮件接收人表 (email_recipient)
```sql
CREATE TABLE IF NOT EXISTS `email_recipient` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '接收人姓名',
    email VARCHAR(100) NOT NULL COMMENT '接收人邮箱地址',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用 (1:启用, 0:禁用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_email (email)
) COMMENT '邮件接收人表';
```

## 📞 技术支持

如遇到问题，请检查：

1. 数据库连接是否正常
2. 邮件配置是否正确
3. DeepSeek AI配置是否正确
4. 网络是否可以访问相关网站
5. 查看应用日志中的错误信息

## 📈 系统特点

- ✅ 自动化监控，无需人工干预
- ✅ 多种监控规则，覆盖不同场景
- ✅ 邮件和微信双重通知机制
- ✅ 完善的Web界面，方便测试和管理
- ✅ 可扩展设计，易于添加新功能
- ✅ 完善的日志记录，便于问题排查
- ✅ 完整的测试覆盖，保证代码质量
- ✅ 支持动态管理监控基金列表
- ✅ 支持Docker容器化部署
- ✅ 数据库存储，持久化配置
- ✅ 响应式邮件设计，支持移动端浏览
- ✅ 集成AI智能助手，提供自然语言交互能力
- ✅ 敏感信息隔离，提高安全性