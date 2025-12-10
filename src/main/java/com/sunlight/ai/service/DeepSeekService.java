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
                    .connectTimeout(60, TimeUnit.SECONDS)  // 增加连接超时时间
                    .writeTimeout(60, TimeUnit.SECONDS)    // 增加写入超时时间
                    .readTimeout(60, TimeUnit.SECONDS)     // 增加读取超时时间
                    .build();
            
            // 构建请求参数
            JSONObject requestBody = buildRequestBody(question);
            
            // 打印请求内容和估算的token数量
            int requestTokens = estimateTokenCount(question);
            logger.info("AI请求: 问题长度={}字符, 估算token数量={}", question.length(), requestTokens);
            
            Request request = new Request.Builder()
                    .url(deepSeekConfig.getApiUrl())
                    .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                    .build();

            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("DeepSeek API请求失败: {}", response.code());
                    return "抱歉，API请求失败,AI服务暂时不可用";
                }
                
                ResponseBody responseBody = response.body();

                if (responseBody == null) {
                    logger.error("DeepSeek API响应为空");
                    return "抱歉，API响应为空,AI服务暂时不可用";
                }
                
                String responseString = responseBody.string();
                JSONObject responseObject = JSON.parseObject(responseString);
                
                // 打印响应内容和token使用情况
                JSONObject usage = responseObject.getJSONObject("usage");
                if (usage != null) {
                    int promptTokens = usage.getIntValue("prompt_tokens");
                    int completionTokens = usage.getIntValue("completion_tokens");
                    int totalTokens = usage.getIntValue("total_tokens");
                    logger.info("AI响应: 提示token={}, 完成token={}, 总token={}", promptTokens, completionTokens, totalTokens);
                }
                
                JSONArray choices = responseObject.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    if (message != null) {
                        String content = message.getString("content");
                        logger.info("AI响应内容长度={}字符", content != null ? content.length() : 0);
                        return content;
                    }
                }
                logger.info("DeepSeek API响应: {}", responseString);
                return "抱歉，AI服务暂时不可用";
            }
        } catch (Exception e) {
            logger.error("调用DeepSeek AI接口失败", e);
            return "抱歉，AI服务暂时不可用";
        }
    }
    
    /**
     * 估算文本的token数量（简单估算）
     * 
     * @param text 文本内容
     * @return 估算的token数量
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 简单估算：英文单词约1个token，中文字符约0.6个token
        int englishWords = text.split("\\s+").length;
        int chineseChars = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        return englishWords + (int)(chineseChars * 0.6);
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