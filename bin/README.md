# Linux 启动脚本使用说明

## 目录结构

```
bin/
├── start.sh          # 应用管理脚本（手动运行）
├── app.sh            # 应用启动脚本（Systemd使用）
├── fund-monitor.service  # Systemd服务配置文件
└── install.sh        # 自动安装脚本
```

## 使用方式

### 方式一：手动管理（适合开发测试）

1. 构建项目：
```bash
mvn clean package
```

2. 给脚本添加执行权限：
```bash
chmod +x bin/start.sh
```

3. 启动应用：
```bash
./bin/start.sh start
```

4. 其他命令：
```bash
./bin/start.sh stop      # 停止应用
./bin/start.sh restart   # 重启应用
./bin/start.sh status    # 查看状态
./bin/start.sh logs      # 查看日志
```

### 方式二：Systemd服务管理（推荐用于生产环境）

1. 构建项目：
```bash
mvn clean package
```

2. 使用安装脚本安装服务：
```bash
sudo bin/install.sh install
```

3. 管理服务：
```bash
sudo systemctl start fund-monitor     # 启动服务
sudo systemctl stop fund-monitor      # 停止服务
sudo systemctl restart fund-monitor   # 重启服务
sudo systemctl status fund-monitor    # 查看状态
sudo journalctl -u fund-monitor -f    # 查看实时日志
```

### 方式三：手动配置Systemd服务

1. 构建项目：
```bash
mvn clean package
```

2. 创建应用目录并复制文件：
```bash
sudo mkdir -p /opt/fund-monitor
sudo cp -r ./* /opt/fund-monitor/
```

3. 创建专用用户：
```bash
sudo groupadd app
sudo useradd -r -g app -d /opt/fund-monitor -s /sbin/nologin app
```

4. 设置权限：
```bash
sudo chown -R app:app /opt/fund-monitor
sudo chmod +x /opt/fund-monitor/bin/app.sh
```

5. 安装Systemd服务：
```bash
sudo cp bin/fund-monitor.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable fund-monitor
```

6. 启动服务：
```bash
sudo systemctl start fund-monitor
```

## 脚本说明

### start.sh
用于手动管理应用的脚本，提供启动、停止、重启、状态查看和日志查看功能。

### app.sh
专为Systemd设计的简化版启动脚本，由Systemd服务直接调用。

### fund-monitor.service
Systemd服务配置文件，定义了如何启动、停止和管理应用。

### install.sh
自动化安装脚本，可以一键安装应用和Systemd服务。

## 注意事项

1. 确保系统已安装Java 8或更高版本
2. 生产环境建议使用Systemd方式进行部署
3. 日志文件默认存储在 `/var/log/fund-monitor/` 目录下
4. 应用默认监听8080端口，可通过修改配置文件进行更改
5. 如需自定义JVM参数，可在Systemd服务文件中修改Environment配置