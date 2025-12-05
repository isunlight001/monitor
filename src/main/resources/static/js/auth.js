// 登录检查功能
function checkLogin() {
    // 检查用户是否已登录
    if (localStorage.getItem('isLoggedIn') !== 'true') {
        window.location.href = 'login.html';
    }
}

// 显示用户名
function displayUsername() {
    const username = localStorage.getItem('username');
    const usernameDisplay = document.getElementById('usernameDisplay');
    if (username && usernameDisplay) {
        usernameDisplay.textContent = '欢迎您，' + username;
    }
}

// 登出功能
function setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            // 清除登录状态
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('username');
            
            // 跳转到登录页面
            window.location.href = 'login.html';
        });
    }
}

// 初始化认证功能
function initAuth() {
    checkLogin();
    displayUsername();
    setupLogout();
}

// 页面加载完成后初始化认证功能
document.addEventListener('DOMContentLoaded', function() {
    initAuth();
});