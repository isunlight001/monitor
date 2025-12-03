package com.sunlight.invest.notification.service;

import com.sunlight.invest.notification.dto.NotificationRequest;
import com.sunlight.invest.notification.dto.NotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统一通知服务
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private WeChatNotificationService weChatNotificationService;

    /**
     * 发送通知
     */
    public NotificationResponse sendNotification(NotificationRequest request) {
        logger.info("收到通知请求: {}", request);

        try {
            switch (request.getType()) {
                case EMAIL:
                    return sendEmailOnly(request);
                case WECHAT:
                    return sendWeChatOnly(request);
                case ALL:
                    return sendAll(request);
                default:
                    return NotificationResponse.failure("不支持的通知类型: " + request.getType());
            }
        } catch (Exception e) {
            logger.error("发送通知失败: {}", e.getMessage(), e);
            return NotificationResponse.failure("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 仅发送邮件
     */
    private NotificationResponse sendEmailOnly(NotificationRequest request) {
        if (!emailNotificationService.isAvailable()) {
            return NotificationResponse.failure("邮件服务不可用，请检查配置");
        }

        emailNotificationService.sendSimpleEmail(
                request.getRecipient(),
                request.getTitle(),
                request.getContent()
        );
        return NotificationResponse.success("邮件发送成功");
    }

    /**
     * 仅发送微信
     */
    private NotificationResponse sendWeChatOnly(NotificationRequest request) {
        if (!weChatNotificationService.isAvailable()) {
            return NotificationResponse.failure("微信服务不可用，请检查配置");
        }

        weChatNotificationService.sendNotification(
                request.getTitle(),
                request.getContent()
        );
        return NotificationResponse.success("微信通知发送成功");
    }

    /**
     * 发送所有通知
     */
    private NotificationResponse sendAll(NotificationRequest request) {
        StringBuilder message = new StringBuilder();
        boolean hasSuccess = false;
        boolean hasFailure = false;

        // 尝试发送邮件
        if (emailNotificationService.isAvailable()) {
            try {
                emailNotificationService.sendSimpleEmail(
                        request.getRecipient(),
                        request.getTitle(),
                        request.getContent()
                );
                message.append("邮件发送成功; ");
                hasSuccess = true;
            } catch (Exception e) {
                message.append("邮件发送失败: ").append(e.getMessage()).append("; ");
                hasFailure = true;
            }
        } else {
            message.append("邮件服务不可用; ");
        }

        // 尝试发送微信
        if (weChatNotificationService.isAvailable()) {
            try {
                weChatNotificationService.sendNotification(
                        request.getTitle(),
                        request.getContent()
                );
                message.append("微信通知发送成功; ");
                hasSuccess = true;
            } catch (Exception e) {
                message.append("微信通知发送失败: ").append(e.getMessage()).append("; ");
                hasFailure = true;
            }
        } else {
            message.append("微信服务不可用; ");
        }

        if (hasSuccess && !hasFailure) {
            return NotificationResponse.success(message.toString());
        } else if (hasSuccess) {
            return new NotificationResponse(true, message.toString());
        } else {
            return NotificationResponse.failure(message.toString());
        }
    }

    /**
     * 检查服务状态
     */
    public NotificationResponse checkStatus() {
        StringBuilder status = new StringBuilder();
        status.append("邮件服务: ").append(emailNotificationService.isAvailable() ? "可用" : "不可用").append("; ");
        status.append("微信服务: ").append(weChatNotificationService.isAvailable() ? "可用" : "不可用");
        
        return NotificationResponse.success(status.toString());
    }
}
