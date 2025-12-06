package com.sunlight.invest.system.service;

import com.sunlight.invest.system.entity.User;
import com.sunlight.invest.system.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 用户初始化器
 * 在应用启动时初始化默认用户
 *
 * @author System
 * @since 2024-12-06
 */
@Component
public class UserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserInitializer.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化默认用户...");
        
        // 检查并创建默认管理员用户
        User adminUser = userMapper.selectByUsername("admin");
        if (adminUser == null) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword("admin123"); // 默认密码
            user.setEmail("admin@example.com");
            user.setRealName("系统管理员");
            user.setEnabled(true);
            
            try {
                userMapper.insert(user);
                log.info("初始化默认管理员用户: username=admin, password=admin123");
            } catch (Exception e) {
                log.error("创建默认管理员用户失败", e);
            }
        } else {
            log.info("默认管理员用户已存在");
        }
        
        log.info("用户初始化完成");
    }
}