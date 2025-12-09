#!/bin/bash

# 应用启动脚本 (适用于Systemd)
APP_NAME="fund-monitor"
APP_VERSION="1.0-SNAPSHOT"
JAR_PATH="/opt/fund-monitor/target/${APP_NAME}-${APP_VERSION}.jar"
LOG_FILE="/var/log/${APP_NAME}/${APP_NAME}.log"
JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# 检查Java是否安装
check_java() {
    if ! command -v java &> /dev/null; then
        echo "错误: 未找到Java，请先安装JDK 8或更高版本"
        exit 1
    fi
}

# 检查应用JAR文件是否存在
check_jar() {
    if [ ! -f "$JAR_PATH" ]; then
        echo "错误: 应用JAR文件不存在: $JAR_PATH"
        exit 1
    fi
}

# 启动应用
start_app() {
    check_java
    check_jar
    
    # 创建日志目录
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # 启动应用
    echo "正在启动 ${APP_NAME}..."
    java $JVM_OPTS -jar "$JAR_PATH"
}

# 主函数
main() {
    case "$1" in
        start)
            start_app
            ;;
        *)
            start_app
            ;;
    esac
}

# 执行主函数
main "$@"