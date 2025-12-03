package com.sunlight.invest.notification.service;

import com.sunlight.invest.notification.config.NotificationProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 微信通知服务
 * 支持Server酱和企业微信
 */
@Service
public class WeChatNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatNotificationService.class);

    private static final String SERVER_CHAN_URL = "https://sctapi.ftqq.com/";
    private static final String CORP_WECHAT_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String CORP_WECHAT_SEND_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send";

    @Autowired
    private NotificationProperties notificationProperties;

    private final OkHttpClient httpClient;

    public WeChatNotificationService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 通过Server酱发送微信通知
     */
    public void sendServerChanNotification(String title, String content) {
        if (!notificationProperties.getWechat().isEnabled()) {
            logger.warn("微信通知未启用");
            return;
        }

        String serverChanKey = notificationProperties.getWechat().getServerChanKey();
        if (serverChanKey == null || serverChanKey.isEmpty() || "YOUR_SERVER_CHAN_KEY".equals(serverChanKey)) {
            logger.error("Server酱KEY未配置，请在application.yml中配置notification.wechat.server-chan-key");
            throw new RuntimeException("Server酱KEY未配置");
        }

        String url = SERVER_CHAN_URL + serverChanKey + ".send";

        try {
            RequestBody body = new FormBody.Builder()
                    .add("title", title)
                    .add("desp", content)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    logger.info("Server酱通知发送成功: title={}, response={}", title, responseBody);
                } else {
                    logger.error("Server酱通知发送失败: code={}, message={}", response.code(), response.message());
                    throw new RuntimeException("Server酱通知发送失败: " + response.message());
                }
            }
        } catch (IOException e) {
            logger.error("Server酱通知发送异常: title={}, error={}", title, e.getMessage(), e);
            throw new RuntimeException("Server酱通知发送异常: " + e.getMessage(), e);
        }
    }

    /**
     * 通过企业微信发送通知
     */
    public void sendCorpWeChatNotification(String title, String content) {
        if (!notificationProperties.getWechat().isEnabled()) {
            logger.warn("微信通知未启用");
            return;
        }

        String corpId = notificationProperties.getWechat().getCorpId();
        String corpSecret = notificationProperties.getWechat().getCorpSecret();
        String agentId = notificationProperties.getWechat().getAgentId();

        if (corpId == null || corpSecret == null || agentId == null ||
                "YOUR_CORP_ID".equals(corpId) || "YOUR_CORP_SECRET".equals(corpSecret)) {
            logger.error("企业微信配置未完成，请检查配置文件");
            throw new RuntimeException("企业微信配置未完成");
        }

        try {
            // 获取access_token
            String accessToken = getCorpWeChatAccessToken(corpId, corpSecret);

            // 发送消息
            String messageUrl = CORP_WECHAT_SEND_URL + "?access_token=" + accessToken;
            
            String jsonBody = String.format(
                    "{\"touser\":\"@all\",\"msgtype\":\"text\",\"agentid\":%s,\"text\":{\"content\":\"%s\\n\\n%s\"}}",
                    agentId, title, content
            );

            RequestBody body = RequestBody.create(
                    jsonBody,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(messageUrl)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    logger.info("企业微信通知发送成功: title={}, response={}", title, responseBody);
                } else {
                    logger.error("企业微信通知发送失败: code={}, message={}", response.code(), response.message());
                    throw new RuntimeException("企业微信通知发送失败: " + response.message());
                }
            }
        } catch (Exception e) {
            logger.error("企业微信通知发送异常: title={}, error={}", title, e.getMessage(), e);
            throw new RuntimeException("企业微信通知发送异常: " + e.getMessage(), e);
        }
    }

    /**
     * 获取企业微信access_token
     */
    private String getCorpWeChatAccessToken(String corpId, String corpSecret) throws IOException {
        String url = CORP_WECHAT_TOKEN_URL + "?corpid=" + corpId + "&corpsecret=" + corpSecret;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // 简单解析JSON，实际项目中建议使用JSON库
                int startIndex = responseBody.indexOf("\"access_token\":\"") + 16;
                int endIndex = responseBody.indexOf("\"", startIndex);
                return responseBody.substring(startIndex, endIndex);
            } else {
                throw new IOException("获取access_token失败: " + response.message());
            }
        }
    }

    /**
     * 发送微信通知（优先使用Server酱）
     */
    public void sendNotification(String title, String content) {
        String serverChanKey = notificationProperties.getWechat().getServerChanKey();
        
        // 优先使用Server酱
        if (serverChanKey != null && !serverChanKey.isEmpty() && !"YOUR_SERVER_CHAN_KEY".equals(serverChanKey)) {
            sendServerChanNotification(title, content);
        } else {
            // 否则尝试企业微信
            sendCorpWeChatNotification(title, content);
        }
    }

    /**
     * 检查微信通知服务是否可用
     */
    public boolean isAvailable() {
        if (!notificationProperties.getWechat().isEnabled()) {
            return false;
        }

        String serverChanKey = notificationProperties.getWechat().getServerChanKey();
        boolean hasServerChan = serverChanKey != null && !serverChanKey.isEmpty() && 
                !"YOUR_SERVER_CHAN_KEY".equals(serverChanKey);

        String corpId = notificationProperties.getWechat().getCorpId();
        boolean hasCorpWeChat = corpId != null && !corpId.isEmpty() && !"YOUR_CORP_ID".equals(corpId);

        return hasServerChan || hasCorpWeChat;
    }
}
