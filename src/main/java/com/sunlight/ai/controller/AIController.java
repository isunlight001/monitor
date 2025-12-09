package com.sunlight.ai.controller;

import com.sunlight.ai.service.DeepSeekService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    
    private static final Logger logger = LoggerFactory.getLogger(AIController.class);
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    /**
     * 调用DeepSeek AI进行问答
     * 
     * @param question 用户问题
     * @return AI回答
     */
    @PostMapping("/chat")
    public Map<String, Object> chatWithAI(@RequestParam String question) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = deepSeekService.getAIResponse(question);
            result.put("success", true);
            result.put("data", response);
            result.put("message", "请求成功");
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            result.put("success", false);
            result.put("message", "服务暂时不可用");
        }
        return result;
    }
    
    /**
     * 调用DeepSeek AI进行问答（JSON格式请求体）
     * 
     * @param request 请求体，包含question字段
     * @return AI回答
     */
    @PostMapping("/chat/json")
    public Map<String, Object> chatWithAIJson(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "问题不能为空");
                return result;
            }
            
            String response = deepSeekService.getAIResponse(question);
            result.put("success", true);
            result.put("data", response);
            result.put("message", "请求成功");
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            result.put("success", false);
            result.put("message", "服务暂时不可用");
        }
        return result;
    }
}