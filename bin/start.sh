#!/bin/bash

# 应用名称
APP_NAME="fund-monitor"
# 应用版本
APP_VERSION="1.0-SNAPSHOT"
# JAR文件路径
JAR_PATH="target/${APP_NAME}-${APP_VERSION}.jar"
# PID文件路径
PID_FILE="/var/run/${APP_NAME}.pid"
# 日志文件路径
LOG_FILE="logs/${APP_NAME}.log"
# JVM参数
JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# 检查是否以root权限运行
check_root() {
    if [ "$EUID" -eq 0 ]; then
        echo "请勿以root用户运行此脚本"
        exit 1
    fi
}

# 检查Java是否安装
check_java() {
    if ! command -v java &> /dev/null; then
        echo "错误: 未找到Java，请先安装JDK 8或更高版本"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "检测到Java版本: $java_version"
}

# 检查应用JAR文件是否存在
check_jar() {
    if [ ! -f "$JAR_PATH" ]; then
        echo "错误: 应用JAR文件不存在，请先构建项目: $JAR_PATH"
        exit 1
    fi
}

# 启动应用
start_app() {
    check_root
    check_java
    check_jar
    
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo "${APP_NAME} 已经在运行中 (PID: $pid)"
            exit 1
        else
            # PID文件存在但进程不存在，删除PID文件
            rm -f "$PID_FILE"
        fi
    fi
    
    # 创建日志目录
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # 启动应用
    echo "正在启动 ${APP_NAME}..."
    nohup java $JVM_OPTS -jar "$JAR_PATH" > "$LOG_FILE" 2>&1 &
    local pid=$!
    echo $pid > "$PID_FILE"
    
    # 等待几秒钟检查应用是否成功启动
    sleep 5
    
    if ps -p "$pid" > /dev/null 2>&1; then
        echo "${APP_NAME} 启动成功 (PID: $pid)"
        echo "日志文件: $LOG_FILE"
    else
        echo "错误: ${APP_NAME} 启动失败"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# 停止应用
stop_app() {
    if [ ! -f "$PID_FILE" ]; then
        echo "${APP_NAME} 未运行"
        exit 1
    fi
    
    local pid=$(cat "$PID_FILE")
    if ps -p "$pid" > /dev/null 2>&1; then
        echo "正在停止 ${APP_NAME} (PID: $pid)..."
        kill "$pid"
        
        # 等待应用停止
        local count=0
        while ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            count=$((count + 1))
            if [ $count -gt 30 ]; then
                echo "停止超时，强制杀死进程..."
                kill -9 "$pid"
                break
            fi
        done
        
        rm -f "$PID_FILE"
        echo "${APP_NAME} 已停止"
    else
        echo "${APP_NAME} 未运行"
        rm -f "$PID_FILE"
    fi
}

# 重启应用
restart_app() {
    stop_app
    sleep 2
    start_app
}

# 检查应用状态
status_app() {
    if [ ! -f "$PID_FILE" ]; then
        echo "${APP_NAME} 未运行"
        exit 1
    fi
    
    local pid=$(cat "$PID_FILE")
    if ps -p "$pid" > /dev/null 2>&1; then
        echo "${APP_NAME} 正在运行 (PID: $pid)"
    else
        echo "${APP_NAME} 未运行"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# 查看日志
show_logs() {
    if [ ! -f "$LOG_FILE" ]; then
        echo "日志文件不存在: $LOG_FILE"
        exit 1
    fi
    
    tail -f "$LOG_FILE"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "命令:"
    echo "  start   - 启动应用"
    echo "  stop    - 停止应用"
    echo "  restart - 重启应用"
    echo "  status  - 查看应用状态"
    echo "  logs    - 查看应用日志"
    echo ""
    echo "示例:"
    echo "  $0 start"
    echo "  $0 stop"
    echo "  $0 restart"
    echo "  $0 status"
    echo "  $0 logs"
}

# 主函数
main() {
    case "$1" in
        start)
            start_app
            ;;
        stop)
            stop_app
            ;;
        restart)
            restart_app
            ;;
        status)
            status_app
            ;;
        logs)
            show_logs
            ;;
        *)
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"