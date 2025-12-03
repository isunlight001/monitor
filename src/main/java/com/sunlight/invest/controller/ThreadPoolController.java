package com.sunlight.invest.controller;

import com.sunlight.invest.service.ThreadPoolService;
import com.sunlight.invest.service.DatabaseSimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/threadpool")
/**
 * 线程池管理接口：提交任务与查询状态
 */
public class ThreadPoolController {
    @Autowired
    private ThreadPoolService service;
    
    @Autowired
    private DatabaseSimulationService databaseSimulationService;

    @PostMapping("/submit")
    public String submitTask() {
        service.submitTask();
        return "任务已提交";
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> map = new HashMap<>();
        map.put("activeCount", service.getActiveCount());
        map.put("queueSize", service.getQueueSize());
        map.put("poolSize", service.getPoolSize());
        map.put("dbRequests", databaseSimulationService.getRequestCount());
        return map;
    }
}