package com.sunlight.ai.controller;

import com.sunlight.ai.service.DeepSeekService;
import com.sunlight.invest.common.BaseResponse;
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
    public BaseResponse<String> chatWithAI(@RequestParam String question) {
        try {
            String response = deepSeekService.getAIResponse(question);
            return BaseResponse.success(response);
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            return BaseResponse.error("服务暂时不可用");
        }
    }
    
    /**
     * 调用DeepSeek AI进行问答（JSON格式请求体）
     * 
     * @param request 请求体，包含question字段
     * @return AI回答
     */
    @PostMapping("/chat/json")
    public BaseResponse<String> chatWithAIJson(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                return BaseResponse.error("问题不能为空");
            }
            
            String response = deepSeekService.getAIResponse(question);
            return BaseResponse.success(response);
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            return BaseResponse.error("服务暂时不可用");
        }
    }
}