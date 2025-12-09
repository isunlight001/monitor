package com.sunlight.ai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunlight.ai.config.DeepSeekConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DeepSeekService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);
    
    @Autowired
    private DeepSeekConfig deepSeekConfig;
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    
    /**
     * 向DeepSeek AI发送文本并获取回复
     * 
     * @param question 用户提问
     * @return AI回复内容
     */
    public String getAIResponse(String question) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // 构建请求参数
            JSONObject requestBody = buildRequestBody(question);
            
            Request request = new Request.Builder()
                    .url(deepSeekConfig.getApiUrl())
                    .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("DeepSeek API请求失败: {}", response.code());
                    return "抱歉，AI服务暂时不可用";
                }
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    logger.error("DeepSeek API响应为空");
                    return "抱歉，AI服务暂时不可用";
                }
                
                String responseString = responseBody.string();
                JSONObject responseObject = JSON.parseObject(responseString);
                
                JSONArray choices = responseObject.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    if (message != null) {
                        return message.getString("content");
                    }
                }
                
                return "抱歉，AI服务暂时不可用";
            }
        } catch (Exception e) {
            logger.error("调用DeepSeek AI接口失败", e);
            return "抱歉，AI服务暂时不可用";
        }
    }
    
    /**
     * 构建请求JSON
     * 
     * @param question 用户问题
     * @return 请求JSON对象
     */
    private JSONObject buildRequestBody(String question) {
        JSONObject requestBody = new JSONObject();
        
        // 设置模型
        requestBody.put("model", deepSeekConfig.getModel());
        
        // 设置消息
        JSONArray messages = new JSONArray();
        
        // 系统消息（可选）
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个 helpful assistant.");
        messages.add(systemMessage);
        
        // 用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        // 设置其他参数
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1024);
        
        return requestBody;
    }
}