#!/bin/bash

# 项目部署脚本
# 功能：从GitHub拉取代码，编译项目，停止旧服务，启动新服务

# 设置变量
PROJECT_NAME="monitor"
PROJECT_DIR="/home/apps/code/$PROJECT_NAME"
GITHUB_URL="https://github.com/isunlight001/monitor.git"  # 请替换为实际的GitHub仓库地址
JAVA_OPTS="-Xms512m -Xmx1024m"
PORT=8080
START_TIME=$(date '+%Y-%m-%d %H:%M:%S')

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 彩色日志函数
info() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] \033[32m✅ $1\033[0m"  # 绿色
}

warn() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] \033[33m⚠️  $1\033[0m"  # 黄色
}

error() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] \033[31m❌ $1\033[0m"  # 红色
}

debug() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] \033[36m🔍 $1\033[0m"  # 青色
}

# 打印启动信息
print_startup_info() {
    echo "=========================================================="
    echo "  基金监控系统部署脚本"
    echo "  项目名称: $PROJECT_NAME"
    echo "  项目路径: $PROJECT_DIR"
    echo "  启动时间: $START_TIME"
    echo "  操作系统: $(uname -s)"
    echo "  用户名: $(whoami)"
    echo "=========================================================="
}

# 检查是否以root权限运行
check_root() {
    if [[ $EUID -eq 0 ]]; then
        warn "不建议以root用户运行此脚本"
    fi
}

# 检查Java环境
check_java() {
    debug "正在检查Java环境..."
    if ! command -v java &> /dev/null; then
        error "未找到Java环境，请先安装JDK"
        error "错误详情: Java环境变量未正确配置或未安装JDK"
        error "请确保已安装JDK 8或更高版本，并已配置JAVA_HOME环境变量"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    info "Java环境检查通过"
    debug "Java版本: $JAVA_VERSION"
}

# 检查Maven环境
check_maven() {
    debug "正在检查Maven环境..."
    if ! command -v mvn &> /dev/null; then
        error "未找到Maven，请先安装Maven"
        error "错误详情: Maven环境变量未正确配置或未安装Maven"
        error "请确保已安装Maven 3.6或更高版本，并已配置MAVEN_HOME环境变量"
        exit 1
    fi
    
    MAVEN_VERSION=$(mvn -v | head -n 1 | awk '{print $3}')
    info "Maven环境检查通过"
    debug "Maven版本: $MAVEN_VERSION"
}

# 检查Git环境
check_git() {
    debug "正在检查Git环境..."
    if ! command -v git &> /dev/null; then
        error "未找到Git，请先安装Git"
        error "错误详情: Git未正确安装"
        exit 1
    fi
    
    GIT_VERSION=$(git --version | awk '{print $3}')
    info "Git环境检查通过"
    debug "Git版本: $GIT_VERSION"
}

# 克隆或更新代码
clone_or_pull() {
    if [ -d "$PROJECT_DIR" ]; then
        info "📁 项目目录已存在，正在更新代码..."
        cd "$PROJECT_DIR"
        if git pull origin main; then
            info "代码更新成功"
        else
            error "Git pull失败"
            error "错误详情: 网络连接问题或权限不足"
            exit 1
        fi
    else
        info "📥 正在克隆项目..."
        if git clone "$GITHUB_URL" "$PROJECT_DIR"; then
            info "项目克隆成功"
            cd "$PROJECT_DIR"
        else
            error "Git clone失败"
            error "错误详情: 网络连接问题或仓库地址错误"
            exit 1
        fi
    fi
}

# 编译项目
build_project() {
    info "🔨 正在清理旧的构建..."
    if ! mvn clean; then
        error "Maven clean失败"
        error "错误详情: 请检查项目pom.xml文件或磁盘空间"
        error "建议解决方案:"
        error "1. 检查pom.xml文件是否有语法错误"
        error "2. 确保target目录有写入权限"
        error "3. 检查磁盘空间是否充足"
        exit 1
    fi
    
    info "🔨 正在编译项目..."
    if ! mvn package -DskipTests; then
        error "Maven编译失败"
        error "错误详情: 请检查源代码是否有编译错误"
        error "建议解决方案:"
        error "1. 运行 mvn compile 查看详细错误信息"
        error "2. 检查Java版本兼容性"
        error "3. 检查依赖包是否下载完整"
        exit 1
    fi
    
    info "项目编译成功"
}

# 查找并停止正在运行的服务
stop_service() {
    info "🛑 正在查找并停止端口 $PORT 上运行的服务..."
    
    # 查找占用指定端口的进程
    PID=$(lsof -t -i:$PORT 2>/dev/null)
    
    if [ -n "$PID" ]; then
        debug "🔍 找到进程PID: $PID，正在停止..."
        if kill -15 $PID; then
            info "进程 $PID 已停止"
        else
            warn "停止进程 $PID 失败，可能进程已退出"
        fi
        
        # 等待进程完全停止
        TIMEOUT=30
        while [ $TIMEOUT -gt 0 ] && kill -0 $PID 2>/dev/null; do
            sleep 1
            ((TIMEOUT--))
        done
        
        # 如果进程仍未停止，强制杀死
        if kill -0 $PID 2>/dev/null; then
            warn "进程 $PID 未正常停止，正在强制终止..."
            kill -9 $PID
        fi
        
        info "服务已停止"
    else
        debug "ℹ️  未找到占用端口 $PORT 的进程"
    fi
}

# 启动服务
start_service() {
    info "▶️ 正在启动服务..."
    
    # 查找生成的jar文件
    JAR_FILE=$(find target -name "*.jar" | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        error "未找到可执行的jar文件"
        error "错误详情: 编译可能未生成jar文件"
        error "建议解决方案:"
        error "1. 检查pom.xml中是否配置了spring-boot-maven-plugin插件"
        error "2. 运行 mvn package 查看详细构建信息"
        exit 1
    fi
    
    info "找到可执行文件: $JAR_FILE"
    
    # 后台启动服务
    nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$PROJECT_DIR/app.log" 2>&1 &
    NEW_PID=$!
    
    debug "⏳ 等待服务启动..."
    sleep 10
    
    # 检查服务是否成功启动
    if kill -0 $NEW_PID 2>/dev/null; then
        info "服务启动成功，PID: $NEW_PID"
        debug "日志文件: $PROJECT_DIR/app.log"
    else
        error "服务启动失败"
        error "建议检查:"
        error "1. 查看日志文件 $PROJECT_DIR/app.log"
        error "2. 检查端口 $PORT 是否被其他程序占用"
        error "3. 检查数据库连接配置是否正确"
        exit 1
    fi
}

# 检查服务状态
check_status() {
    debug "🔍 正在检查服务状态..."
    
    # 检查端口是否被监听
    if netstat -tuln | grep -q ":$PORT "; then
        info "服务已在端口 $PORT 上运行"
        debug "本地访问地址: http://localhost:$PORT"
    else
        warn "服务可能未在端口 $PORT 上运行"
        warn "建议检查:"
        warn "1. 查看日志文件 $PROJECT_DIR/app.log"
        warn "2. 检查端口 $PORT 是否被其他程序占用"
        warn "3. 检查数据库连接配置是否正确"
    fi
}

# 打印完成信息
print_completion_info() {
    END_TIME=$(date '+%Y-%m-%d %H:%M:%S')
    info "🎉 项目部署完成"
    echo "📊 系统信息汇总:"
    echo "📊   启动时间: $START_TIME"
    echo "📊   完成时间: $END_TIME"
    echo "📊   项目名称: $PROJECT_NAME"
    echo "📊   项目路径: $PROJECT_DIR"
    echo "📊   监听端口: $PORT"
    echo "📊   访问地址: http://localhost:$PORT"
    echo "=========================================================="
}

# 主函数
main() {
    print_startup_info
    
    check_root
    check_java
    check_maven
    check_git
    
    clone_or_pull
    build_project
    stop_service
    start_service
    check_status
    
    print_completion_info
}

# 运行主函数
main