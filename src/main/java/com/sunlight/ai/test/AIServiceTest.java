package com.sunlight.ai.test;

import com.sunlight.ai.service.DeepSeekService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AIServiceTest implements CommandLineRunner {
    
    private final DeepSeekService deepSeekService;
    
    public AIServiceTest(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 简单测试AI服务
        System.out.println("=== DeepSeek AI服务测试 ===");
        String question = "你好，你是谁？";
        String response = deepSeekService.getAIResponse(question);
        System.out.println("问题: " + question);
        System.out.println("回答: " + response);
        System.out.println("========================");
    }
}