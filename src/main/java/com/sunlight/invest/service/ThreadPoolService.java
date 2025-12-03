package com.sunlight.invest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
/**
 * 线程池业务服务：提交任务并提供运行指标
 */
public class ThreadPoolService {
    @Autowired
    private ThreadPoolTaskExecutor demoExecutor;

    private AtomicInteger taskCount = new AtomicInteger(0);

    public void submitTask() {
        demoExecutor.submit(() -> {
            int id = taskCount.incrementAndGet();
            try {
                System.out.println("任务" + id + "开始执行");
                Thread.sleep(5000); // 模拟耗时任务
                System.out.println("任务" + id + "执行完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public int getActiveCount() {
        return demoExecutor.getActiveCount();
    }

    public int getQueueSize() {
        return demoExecutor.getThreadPoolExecutor().getQueue().size();
    }

    public int getPoolSize() {
        return demoExecutor.getPoolSize();
    }
} 