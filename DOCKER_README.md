# Docker部署指南

## 项目简介

本项目是一个基金监控系统，可以自动抓取基金数据并监控异常波动。

## Docker部署方式

### 方式一：使用docker-compose（推荐）

1. 确保已安装Docker和docker-compose
2. 在项目根目录下执行以下命令：

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app
```

3. 访问应用：
   - 基金监控页面: http://localhost:8081/fund-monitor.html
   - 数据库管理界面: http://localhost:3306

### 方式二：单独构建和运行

1. 构建Docker镜像：
```bash
docker build -t fund-monitor .
```

2. 运行MySQL数据库：
```bash
docker run -d \
  --name fund-monitor-db \
  -e MYSQL_ROOT_PASSWORD=root123456 \
  -e MYSQL_DATABASE=fund \
  -p 3306:3306 \
  -v db_data:/var/lib/mysql \
  mysql:8.0
```

3. 运行应用：
```bash
docker run -d \
  --name fund-monitor-app \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/fund?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false \
  fund-monitor
```

## 常用命令

```bash
# 停止所有服务
docker-compose down

# 重启服务
docker-compose restart

# 查看容器日志
docker logs fund-monitor-app

# 进入容器内部
docker exec -it fund-monitor-app /bin/bash
```

## 环境变量

应用支持以下环境变量配置：

- `SPRING_DATASOURCE_URL`: 数据库连接URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `JAVA_OPTS`: JVM参数

## 注意事项

1. 首次启动时，数据库会自动初始化表结构
2. 应用默认监听8081端口
3. 可以通过修改docker-compose.yml文件来调整端口映射和其他配置