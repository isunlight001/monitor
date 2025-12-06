// 用户认证相关函数

// 检查登录状态
async function checkLoginStatus() {
    try {
        const response = await fetch('/api/users/status');
        const result = await response.json();
        
        if (result.success) {
            // 显示用户信息
            document.getElementById('userInfo').style.display = 'flex';
            document.getElementById('currentUserName').textContent = result.data.username;
        } else {
            // 未登录，跳转到登录页面
            window.location.href = 'login.html';
        }
    } catch (error) {
        console.error('检查登录状态失败:', error);
        // 跳转到登录页面
        window.location.href = 'login.html';
    }
}

// 用户登出
async function logout() {
    try {
        const response = await fetch('/api/users/logout', {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (response.ok && result.success) {
            // 登出成功，跳转到登录页面
            window.location.href = 'login.html';
        } else {
            alert('登出失败: ' + result.message);
        }
    } catch (error) {
        alert('登出失败: ' + error.message);
    }
}