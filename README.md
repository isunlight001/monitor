# 基金监控与线程池监控系统

## 📋 项目概述

本项目是一个综合性的监控系统，包含两个主要功能模块：

1. **基金监控系统** - 自动抓取基金净值数据并监控异常波动
2. **线程池监控系统** - 监控Spring Boot应用中的线程池状态

## 🏗️ 技术栈

- **后端框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0.33
- **连接池**: Druid
- **ORM框架**: MyBatis
- **邮件服务**: Spring Mail
- **HTTP客户端**: OkHttp3, Jsoup
- **Excel处理**: Apache POI
- **监控**: Spring Boot Actuator, Micrometer Prometheus
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
│       │   └── FundNav.java          # 基金净值实体类
│       ├── mapper/
│       │   └── FundNavMapper.java    # 基金净值Mapper
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

src/main/resources/
├── static/
│   ├── fund-backtest.html            # 基金回测页面
│   ├── fund-monitor.html             # 基金监控页面
│   ├── notification-test.html        # 通知测试页面
│   └── index.html                    # 首页
├── application.yml                   # 应用配置文件
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

#### API接口
- `POST /api/fund/monitor/crawl` - 抓取基金数据
- `POST /api/fund/monitor/update` - 增量更新基金数据
- `POST /api/fund/monitor/check` - 执行监控检查
- `GET /api/fund/monitor/nav` - 查询基金净值

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

#### API接口
- `POST /api/notification/send` - 发送通知
- `GET /api/notification/status` - 检查服务状态
- `POST /api/notification/test/email` - 测试邮件发送
- `POST /api/notification/test/wechat` - 测试微信发送

## ⚙️ 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 邮件配置
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-auth-code
```

### 基金监控配置
```yaml
fund:
  monitor:
    codes: 006195:国金量化多因子
```

## 🛠️ 构建与运行

### 环境要求
- Java 8
- MySQL 8.0.33
- Maven 3.6+

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

## 🌐 访问地址

启动应用后，可通过以下URL访问：

- **首页**: http://localhost/
- **基金回测**: http://localhost/fund-backtest.html
- **基金监控**: http://localhost/fund-monitor.html
- **通知测试**: http://localhost/notification-test.html

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

## 📞 技术支持

如遇到问题，请检查：

1. 数据库连接是否正常
2. 邮件配置是否正确
3. 网络是否可以访问相关网站
4. 查看应用日志中的错误信息

## 📈 系统特点

- ✅ 自动化监控，无需人工干预
- ✅ 多种监控规则，覆盖不同场景
- ✅ 邮件和微信双重通知机制
- ✅ 完善的Web界面，方便测试和管理
- ✅ 可扩展设计，易于添加新功能
- ✅ 完善的日志记录，便于问题排查
- ✅ 完整的测试覆盖，保证代码质量
