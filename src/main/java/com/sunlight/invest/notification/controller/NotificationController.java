package com.sunlight.invest.notification.controller;

import com.sunlight.invest.notification.dto.NotificationRequest;
import com.sunlight.invest.notification.dto.NotificationResponse;
import com.sunlight.invest.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 发送通知
     */
    @PostMapping("/send")
    public NotificationResponse sendNotification(@RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request);
    }

    /**
     * 检查服务状态
     */
    @GetMapping("/status")
    public NotificationResponse checkStatus() {
        return notificationService.checkStatus();
    }

    /**
     * 测试邮件发送
     */
    @PostMapping("/test/email")
    public NotificationResponse testEmail(@RequestParam(required = false) String recipient,
                                         @RequestParam String subject,
                                         @RequestParam String content) {
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationRequest.NotificationType.EMAIL);
        request.setRecipient(recipient);
        request.setTitle(subject);
        request.setContent(content);
        return notificationService.sendNotification(request);
    }

    /**
     * 测试微信发送
     */
    @PostMapping("/test/wechat")
    public NotificationResponse testWeChat(@RequestParam String title,
                                          @RequestParam String content) {
        NotificationRequest request = new NotificationRequest();
        request.setType(NotificationRequest.NotificationType.WECHAT);
        request.setTitle(title);
        request.setContent(content);
        return notificationService.sendNotification(request);
    }
}
