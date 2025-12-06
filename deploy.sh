#!/bin/bash

# 项目部署脚本
# 功能：从GitHub拉取代码，编译项目，停止旧服务，启动新服务

# 设置变量
PROJECT_NAME="monitor"
PROJECT_DIR="/home/apps/code/$PROJECT_NAME"
GITHUB_URL="https://github.com/isunlight001/monitor.git"  # 请替换为实际的GitHub仓库地址
JAVA_OPTS="-Xms512m -Xmx1024m"
PORT=8080

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 检查是否以root权限运行
check_root() {
    if [[ $EUID -eq 0 ]]; then
        log "警告：不建议以root用户运行此脚本"
    fi
}

# 检查Java环境
check_java() {
    if ! command -v java &> /dev/null; then
        log "错误：未找到Java环境，请先安装JDK"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    log "Java版本: $JAVA_VERSION"
}

# 检查Maven环境
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log "错误：未找到Maven，请先安装Maven"
        exit 1
    fi
    
    MAVEN_VERSION=$(mvn -v | head -n 1 | awk '{print $3}')
    log "Maven版本: $MAVEN_VERSION"
}

# 检查Git环境
check_git() {
    if ! command -v git &> /dev/null; then
        log "错误：未找到Git，请先安装Git"
        exit 1
    fi
}

# 克隆或更新代码
clone_or_pull() {
    if [ -d "$PROJECT_DIR" ]; then
        log "项目目录已存在，正在更新代码..."
        cd "$PROJECT_DIR"
        git pull origin main
        if [ $? -ne 0 ]; then
            log "错误：Git pull失败"
            exit 1
        fi
    else
        log "正在克隆项目..."
        git clone "$GITHUB_URL" "$PROJECT_DIR"
        if [ $? -ne 0 ]; then
            log "错误：Git clone失败"
            exit 1
        fi
        cd "$PROJECT_DIR"
    fi
}

# 编译项目
build_project() {
    log "正在清理旧的构建..."
    mvn clean
    if [ $? -ne 0 ]; then
        log "错误：Maven clean失败"
        exit 1
    fi
    
    log "正在编译项目..."
    mvn package -DskipTests
    if [ $? -ne 0 ]; then
        log "错误：Maven编译失败"
        exit 1
    fi
    
    log "项目编译成功"
}

# 查找并停止正在运行的服务
stop_service() {
    log "正在查找并停止端口 $PORT 上运行的服务..."
    
    # 查找占用指定端口的进程
    PID=$(lsof -t -i:$PORT)
    
    if [ -n "$PID" ]; then
        log "找到进程PID: $PID，正在停止..."
        kill -15 $PID
        
        # 等待进程完全停止
        TIMEOUT=30
        while [ $TIMEOUT -gt 0 ] && kill -0 $PID 2>/dev/null; do
            sleep 1
            ((TIMEOUT--))
        done
        
        # 如果进程仍未停止，强制杀死
        if kill -0 $PID 2>/dev/null; then
            log "进程未正常停止，正在强制终止..."
            kill -9 $PID
        fi
        
        log "服务已停止"
    else
        log "未找到占用端口 $PORT 的进程"
    fi
}

# 启动服务
start_service() {
    log "正在启动服务..."
    
    # 查找生成的jar文件
    JAR_FILE=$(find target -name "*.jar" | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        log "错误：未找到可执行的jar文件"
        exit 1
    fi
    
    log "找到可执行文件: $JAR_FILE"
    
    # 后台启动服务
    nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$PROJECT_DIR/app.log" 2>&1 &
    NEW_PID=$!
    
    # 等待几秒钟让服务启动
    sleep 10
    
    # 检查服务是否成功启动
    if kill -0 $NEW_PID 2>/dev/null; then
        log "服务启动成功，PID: $NEW_PID"
        log "日志文件: $PROJECT_DIR/app.log"
    else
        log "错误：服务启动失败"
        exit 1
    fi
}

# 检查服务状态
check_status() {
    log "正在检查服务状态..."
    
    # 检查端口是否被监听
    if netstat -tuln | grep -q ":$PORT "; then
        log "服务已在端口 $PORT 上运行"
    else
        log "警告：服务可能未在端口 $PORT 上运行"
    fi
}

# 主函数
main() {
    log "开始部署项目: $PROJECT_NAME"
    
    check_root
    check_java
    check_maven
    check_git
    
    clone_or_pull
    build_project
    stop_service
    start_service
    check_status
    
    log "项目部署完成"
}

# 运行主函数
main