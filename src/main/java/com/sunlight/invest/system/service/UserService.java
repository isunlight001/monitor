package com.sunlight.invest.system.service;

import com.sunlight.invest.system.entity.User;
import com.sunlight.invest.system.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务类
 *
 * @author System
 * @since 2024-12-06
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户对象，如果登录失败返回null
     */
    public User login(String username, String password) {
        try {
            User user = userMapper.selectByUsername(username);
            if (user != null && user.getPassword().equals(password) && user.getEnabled()) {
                log.info("用户登录成功: {}", username);
                return user;
            } else {
                log.warn("用户登录失败: {}", username);
                return null;
            }
        } catch (Exception e) {
            log.error("用户登录异常: {}", username, e);
            return null;
        }
    }

    /**
     * 添加用户
     *
     * @param user 用户对象
     * @return 添加的记录数
     */
    public int addUser(User user) {
        try {
            // 检查用户名是否已存在
            User existing = userMapper.selectByUsername(user.getUsername());
            if (existing != null) {
                throw new RuntimeException("用户名已存在: " + user.getUsername());
            }

            // 检查邮箱是否已存在
            existing = userMapper.selectByEmail(user.getEmail());
            if (existing != null) {
                throw new RuntimeException("邮箱地址已存在: " + user.getEmail());
            }

            int result = userMapper.insert(user);
            log.info("添加用户成功: {}", user.getUsername());
            return result;
        } catch (Exception e) {
            log.error("添加用户失败: {}", user.getUsername(), e);
            throw new RuntimeException("添加用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    public User getUserById(Long id) {
        try {
            User user = userMapper.selectById(id);
            log.debug("查询用户: ID={}", id);
            return user;
        } catch (Exception e) {
            log.error("查询用户失败: ID={}", id, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    public User getUserByUsername(String username) {
        try {
            User user = userMapper.selectByUsername(username);
            log.debug("查询用户: username={}", username);
            return user;
        } catch (Exception e) {
            log.error("查询用户失败: username={}", username, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有用户
     *
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        try {
            List<User> result = userMapper.selectAll();
            log.debug("查询所有用户，数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("查询所有用户失败", e);
            throw new RuntimeException("查询所有用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 更新的记录数
     */
    public int updateUser(User user) {
        try {
            // 检查用户名是否已存在于其他记录中
            User existing = userMapper.selectByUsername(user.getUsername());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new RuntimeException("用户名已存在: " + user.getUsername());
            }

            // 检查邮箱是否已存在于其他记录中
            existing = userMapper.selectByEmail(user.getEmail());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new RuntimeException("邮箱地址已存在: " + user.getEmail());
            }

            int result = userMapper.update(user);
            log.info("更新用户成功: ID={}", user.getId());
            return result;
        } catch (Exception e) {
            log.error("更新用户失败: ID={}", user.getId(), e);
            throw new RuntimeException("更新用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID删除用户
     *
     * @param id 主键ID
     * @return 删除的记录数
     */
    public int deleteUserById(Long id) {
        try {
            int result = userMapper.deleteById(id);
            log.info("删除用户成功: ID={}", id);
            return result;
        } catch (Exception e) {
            log.error("删除用户失败: ID={}", id, e);
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }
}