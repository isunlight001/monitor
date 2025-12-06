package com.sunlight.invest.system.controller;

import com.sunlight.invest.system.entity.User;
import com.sunlight.invest.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author System
 * @since 2024-12-06
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                result.put("success", false);
                result.put("message", "用户名和密码不能为空");
                return result;
            }

            User user = userService.login(username, password);
            if (user != null) {
                // 将用户信息存储到session中
                session.setAttribute("user", user);
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("data", user);
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("用户登录失败", e);
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            session.invalidate();
            result.put("success", true);
            result.put("message", "登出成功");
        } catch (Exception e) {
            log.error("用户登出失败", e);
            result.put("success", false);
            result.put("message", "登出失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                result.put("success", true);
                result.put("message", "已登录");
                result.put("data", user);
            } else {
                result.put("success", false);
                result.put("message", "未登录");
            }
        } catch (Exception e) {
            log.error("检查登录状态失败", e);
            result.put("success", false);
            result.put("message", "检查登录状态失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 添加用户
     */
    @PostMapping
    public Map<String, Object> addUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = userService.addUser(user);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "用户添加成功");
            } else {
                result.put("success", false);
                result.put("message", "用户添加失败");
            }
        } catch (Exception e) {
            log.error("添加用户失败", e);
            result.put("success", false);
            result.put("message", "添加用户失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Map<String, Object> updateUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = userService.updateUser(user);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "用户更新成功");
            } else {
                result.put("success", false);
                result.put("message", "用户更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            result.put("success", false);
            result.put("message", "更新用户失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据ID删除用户
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUserById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = userService.deleteUserById(id);
            if (count > 0) {
                result.put("success", true);
                result.put("message", "用户删除成功");
            } else {
                result.put("success", false);
                result.put("message", "用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            result.put("success", false);
            result.put("message", "删除用户失败: " + e.getMessage());
        }
        return result;
    }
}