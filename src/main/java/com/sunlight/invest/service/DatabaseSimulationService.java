package com.sunlight.invest.service;

import com.sunlight.invest.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
/**
 * 数据库模拟服务：定时查询与插入，统计请求数
 */
public class DatabaseSimulationService {
    @Autowired
    private UserService userService;

    private AtomicInteger requestCounter = new AtomicInteger(0);
    
    private Random random = new Random();
    
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        // 延迟初始化，避免在数据库表创建前访问数据库
    }

    // 模拟定期的数据库查询操作
    @Scheduled(fixedRate = 1000) // 每秒执行一次
    public void simulateDatabaseQuery() {
        try {
            // 初始化数据（仅执行一次）
            if (!initialized) {
                initializeData();
                initialized = true;
            }
            
            // 随机查询用户
            int totalUsers = 5;
            int userId = random.nextInt(totalUsers) + 1;
            userService.findById((long) userId);
            requestCounter.incrementAndGet();
        } catch (Exception e) {
            // 忽略异常，仅用于模拟
        }
    }

    // 模拟定期的数据库插入操作
    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void simulateDatabaseInsert() {
        try {
            // 确保数据已初始化
            if (!initialized) {
                initializeData();
                initialized = true;
            }
            
            int count = requestCounter.incrementAndGet();
            User user = new User("AutoUser" + count, "auto" + count + "@test.com", 20 + (count % 50));
            userService.insert(user);
        } catch (Exception e) {
            // 忽略异常，仅用于模拟
        }
    }
    
    private void initializeData() {
        try {
            // 尝试插入数据以检查表是否存在
            User testUser = new User("TestUser", "test@example.com", 25);
            userService.insert(testUser);
            
            // 如果没有抛出异常，说明表已存在，检查是否需要初始化数据
            if (userService.findAll().size() <= 1) { // 只有测试用户
                // 初始化一些用户数据
                for (int i = 1; i <= 5; i++) {
                    User user = new User("User" + i, "user" + i + "@example.com", 20 + i);
                    userService.insert(user);
                }
            }
            
            // 删除测试用户
            userService.deleteById(testUser.getId());
        } catch (Exception e) {
            // 如果表不存在或有其他问题，忽略错误
            System.out.println("Database initialization check failed: " + e.getMessage());
        }
    }

    public int getRequestCount() {
        return requestCounter.get();
    }
}