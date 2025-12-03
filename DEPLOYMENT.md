# 部署指南

## 环境准备

### 系统要求
- **操作系统**: Windows/Linux/macOS
- **Java版本**: JDK 8 或更高版本
- **数据库**: MySQL 8.0.33 或更高版本
- **内存**: 最少2GB RAM
- **磁盘空间**: 最少10GB可用空间

### 软件依赖
1. **Java Development Kit (JDK) 8+**
2. **Apache Maven 3.6+**
3. **MySQL 8.0.33+**
4. **Git** (可选，用于代码管理)

## 数据库配置

### 1. 安装MySQL
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# Windows
# 下载并安装MySQL Installer
```

### 2. 创建数据库
```sql
CREATE DATABASE fund_monitor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'fund_user'@'localhost' IDENTIFIED BY 'fund_password';
GRANT ALL PRIVILEGES ON fund_monitor.* TO 'fund_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 初始化表结构
项目启动时会自动执行 `schema.sql` 文件创建表结构。

## 应用配置

### 1. 修改配置文件
编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fund_monitor?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
    username: fund_user
    password: fund_password
    
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-email-password
    
notification:
  mail:
    from: your-email@qq.com
    to: receiver@qq.com
    
fund:
  monitor:
    codes: 006195:国金量化多因子
```

### 2. 邮件配置说明
- **host**: SMTP服务器地址
- **port**: SMTP端口号
- **username**: 发送方邮箱账号
- **password**: 邮箱授权码（不是登录密码）
- **from**: 发件人邮箱
- **to**: 收件人邮箱

## 构建项目

### 1. 克隆代码
```bash
git clone <repository-url>
cd monitor
```

### 2. 清理和编译
```bash
mvn clean compile
```

### 3. 运行测试
```bash
mvn test
```

### 4. 打包应用
```bash
mvn clean package
```

生成的JAR文件位于: `target/monitor-1.0-SNAPSHOT.jar`

## 部署方式

### 方式一：直接运行JAR包

#### 开发环境运行
```bash
mvn spring-boot:run
```

#### 生产环境运行
```bash
java -jar target/monitor-1.0-SNAPSHOT.jar
```

#### 后台运行
```bash
nohup java -jar target/monitor-1.0-SNAPSHOT.jar > app.log 2>&1 &
```

### 方式二：部署到Tomcat

1. 修改 `pom.xml` 打包方式为war:
```xml
<packaging>war</packaging>
```

2. 添加Tomcat依赖:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <scope>provided</scope>
</dependency>
```

3. 打包并部署:
```bash
mvn clean package
cp target/monitor-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/
```

### 方式三：Docker部署

#### 1. 创建Dockerfile
```dockerfile
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/monitor-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

#### 2. 构建镜像
```bash
docker build -t fund-monitor:1.0 .
```

#### 3. 运行容器
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name fund-monitor \
  fund-monitor:1.0
```

## 系统配置

### JVM参数优化
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar target/monitor-1.0-SNAPSHOT.jar
```

### 环境变量配置
```bash
export JAVA_OPTS="-Xms512m -Xmx2g"
export SPRING_PROFILES_ACTIVE=prod
```

### 日志配置
在 `application.yml` 中配置日志:
```yaml
logging:
  level:
    com.sunlight.invest: INFO
    org.springframework: WARN
  file:
    name: logs/app.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 监控配置

### Prometheus集成
1. 添加依赖:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. 配置Prometheus:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
```

3. 访问监控端点:
```
http://localhost:8080/actuator/prometheus
```

## 定时任务配置

### 修改执行时间
在 `FundMonitorScheduler.java` 中修改cron表达式:
```java
@Scheduled(cron = "0 0 23 * * ?") // 每晚11点执行
```

### Cron表达式说明
- `0 0 23 * * ?` - 每天23:00执行
- `0 0 0 * * ?` - 每天00:00执行
- `0 0 0 * * 1` - 每周一00:00执行

## 安全配置

### 端口配置
```yaml
server:
  port: 8080
```

### SSL配置
```yaml
server:
  port: 443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: password
    keyStoreType: PKCS12
    keyAlias: tomcat
```

## 故障排除

### 常见问题

#### 1. 数据库连接失败
检查:
- 数据库服务是否启动
- 连接URL、用户名、密码是否正确
- 防火墙是否允许端口访问

#### 2. 邮件发送失败
检查:
- SMTP服务器配置是否正确
- 邮箱授权码是否正确
- 网络是否可以访问SMTP服务器

#### 3. 定时任务不执行
检查:
- `@EnableScheduling` 注解是否添加
- cron表达式是否正确
- 系统时间是否正确

#### 4. 内存不足
解决方案:
- 增加JVM堆内存: `-Xmx2g`
- 优化数据库查询
- 添加缓存机制

### 日志查看
```bash
# 查看应用日志
tail -f logs/app.log

# 查看系统日志
journalctl -u fund-monitor -f
```

## 性能调优

### 数据库优化
1. 添加索引:
```sql
ALTER TABLE fund_nav ADD INDEX idx_fund_date (fund_code, nav_date);
```

2. 优化查询语句
3. 定期清理历史数据

### JVM优化
```bash
# G1垃圾收集器
-XX:+UseG1GC -XX:MaxGCPauseMillis=200

# 堆内存设置
-Xms1g -Xmx2g

# 新生代设置
-XX:NewRatio=2
```

## 备份与恢复

### 数据库备份
```bash
# 备份
mysqldump -u fund_user -p fund_monitor > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u fund_user -p fund_monitor < backup_20241203.sql
```

### 应用备份
```bash
# 备份JAR文件
cp target/monitor-1.0-SNAPSHOT.jar backups/

# 备份配置文件
cp src/main/resources/application.yml backups/
```

## 升级维护

### 版本升级步骤
1. 停止当前服务
2. 备份现有文件
3. 部署新版本
4. 启动服务
5. 验证功能

### 回滚方案
```bash
# 停止当前服务
kill $(cat app.pid)

# 恢复旧版本
cp backups/monitor-old.jar target/monitor-1.0-SNAPSHOT.jar

# 启动服务
nohup java -jar target/monitor-1.0-SNAPSHOT.jar > app.log 2>&1 &
```
