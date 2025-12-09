#!/bin/bash

# 应用安装脚本
APP_NAME="fund-monitor"
INSTALL_DIR="/opt/${APP_NAME}"
SERVICE_FILE="${APP_NAME}.service"
USER="app"
GROUP="app"

# 检查是否以root权限运行
check_root() {
    if [ "$EUID" -ne 0 ]; then
        echo "请以root权限运行此脚本: sudo $0"
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

# 创建用户和组
create_user_group() {
    echo "创建用户和组: ${USER}:${GROUP}"
    
    # 创建组
    if ! getent group "$GROUP" > /dev/null 2>&1; then
        groupadd "$GROUP"
    fi
    
    # 创建用户
    if ! getent passwd "$USER" > /dev/null 2>&1; then
        useradd -r -g "$GROUP" -d "$INSTALL_DIR" -s /sbin/nologin "$USER"
    fi
}

# 安装应用
install_app() {
    echo "安装应用到: $INSTALL_DIR"
    
    # 创建安装目录
    mkdir -p "$INSTALL_DIR"
    
    # 复制文件（假设脚本在项目根目录的bin目录下运行）
    if [ -d "../target" ]; then
        cp -r ../* "$INSTALL_DIR"/
    else
        echo "错误: 请在项目的bin目录下运行此脚本"
        exit 1
    fi
    
    # 设置权限
    chown -R "$USER":"$GROUP" "$INSTALL_DIR"
    chmod +x "$INSTALL_DIR/bin/start.sh"
    chmod +x "$INSTALL_DIR/bin/app.sh"
    
    echo "应用安装完成"
}

# 安装Systemd服务
install_service() {
    echo "安装Systemd服务"
    
    # 复制服务文件
    cp "$SERVICE_FILE" /etc/systemd/system/
    
    # 重新加载Systemd配置
    systemctl daemon-reload
    
    # 启用服务
    systemctl enable "$APP_NAME"
    
    echo "Systemd服务安装完成"
    echo "使用以下命令管理服务:"
    echo "  启动服务: sudo systemctl start $APP_NAME"
    echo "  停止服务: sudo systemctl stop $APP_NAME"
    echo "  重启服务: sudo systemctl restart $APP_NAME"
    echo "  查看状态: sudo systemctl status $APP_NAME"
    echo "  查看日志: sudo journalctl -u $APP_NAME -f"
}

# 显示帮助信息
show_help() {
    echo "用法: sudo $0 [install|uninstall]"
    echo ""
    echo "命令:"
    echo "  install   - 安装应用"
    echo "  uninstall - 卸载应用"
    echo ""
    echo "示例:"
    echo "  sudo $0 install"
    echo "  sudo $0 uninstall"
}

# 卸载应用
uninstall_app() {
    echo "卸载应用"
    
    # 停止服务
    systemctl stop "$APP_NAME" 2>/dev/null
    
    # 禁用服务
    systemctl disable "$APP_NAME" 2>/dev/null
    
    # 删除服务文件
    rm -f "/etc/systemd/system/$SERVICE_FILE"
    
    # 重新加载Systemd配置
    systemctl daemon-reload
    
    # 删除安装目录
    rm -rf "$INSTALL_DIR"
    
    # 删除用户和组
    userdel "$USER" 2>/dev/null
    groupdel "$GROUP" 2>/dev/null
    
    echo "应用卸载完成"
}

# 主函数
main() {
    check_root
    check_java
    
    case "$1" in
        install)
            create_user_group
            install_app
            install_service
            echo "安装完成！"
            echo "请使用以下命令启动应用:"
            echo "  sudo systemctl start $APP_NAME"
            ;;
        uninstall)
            uninstall_app
            echo "卸载完成！"
            ;;
        *)
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"