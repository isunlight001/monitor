package com.sunlight.invest.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * 邮件通知服务集成测试
 * <p>
 * 实际测试邮件发送功能，需要配置真实的邮件服务器信息
 * </p>
 *
 * @author System
 * @since 2024-12-02
 */
@SpringBootTest
@TestPropertySource(properties = {
    "notification.mail.enabled=true",
    "notification.mail.from=57954282@qq.com",
    "notification.mail.to=903635811@qq.com",
    "spring.mail.host=smtp.qq.com",
    "spring.mail.port=587",
    "spring.mail.username=57954282@qq.com",
    "spring.mail.password=okfrcokdlwlbbbac"
})
public class EmailNotificationServiceIntegrationTest {

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    /**
     * 测试邮件服务是否可用
     * 注意：此测试不会实际发送邮件，只检查服务状态
     */
    @Test
    public void testEmailServiceAvailable() {
        if (emailNotificationService != null) {
            boolean available = emailNotificationService.isAvailable();
            System.out.println("邮件服务状态: " + (available ? "可用" : "不可用"));
        } else {
            System.out.println("邮件服务未初始化");
        }
    }

    /**
     * 测试发送简单文本邮件
     * 注意：需要配置真实的邮件服务器信息才能实际发送
     * 
     * 使用方法：
     * 1. 在 application.yml 中配置真实的邮件信息
     * 2. 取消 @Disabled 注解
     * 3. 运行测试
     */
    // @Disabled("需要配置真实邮件服务器才能运行")
    @Test
    public void testSendSimpleEmailManual() {
        if (emailNotificationService == null) {
            System.out.println("邮件服务未初始化，跳过测试");
            return;
        }

        if (!emailNotificationService.isAvailable()) {
            System.out.println("邮件服务不可用，请检查配置");
            return;
        }

        try {
            emailNotificationService.sendSimpleEmail(
                null,  // 使用默认收件人
                "测试邮件 - " + System.currentTimeMillis(),
                "这是一封测试邮件\n\n发送时间: " + java.time.LocalDateTime.now()
            );
            System.out.println("测试邮件发送成功！");
        } catch (Exception e) {
            System.err.println("测试邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试发送HTML邮件
     */
    // @Disabled("需要配置真实邮件服务器才能运行")
    @Test
    public void testSendHtmlEmailManual() {
        if (emailNotificationService == null || !emailNotificationService.isAvailable()) {
            System.out.println("邮件服务不可用，跳过测试");
            return;
        }

        try {
            String htmlContent = "<html><body>" +
                "<h1 style='color: #4CAF50;'>测试HTML邮件</h1>" +
                "<p>这是一封<strong>HTML格式</strong>的测试邮件。</p>" +
                "<ul>" +
                "<li>功能1: 简单文本邮件</li>" +
                "<li>功能2: HTML格式邮件</li>" +
                "<li>功能3: 自动配置收发件人</li>" +
                "</ul>" +
                "<p>发送时间: " + java.time.LocalDateTime.now() + "</p>" +
                "</body></html>";

            emailNotificationService.sendHtmlEmail(
                null,  // 使用默认收件人
                "HTML测试邮件 - " + System.currentTimeMillis(),
                htmlContent
            );
            System.out.println("HTML测试邮件发送成功！");
        } catch (Exception e) {
            System.err.println("HTML测试邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试发送到指定收件人
     */
    // @Disabled("需要配置真实邮件服务器才能运行")
    @Test
    public void testSendToSpecificRecipient() {
        if (emailNotificationService == null || !emailNotificationService.isAvailable()) {
            System.out.println("邮件服务不可用，跳过测试");
            return;
        }

        try {
            // 修改为实际的收件人邮箱
            String specificRecipient = "903635811@qq.com";
            
            emailNotificationService.sendSimpleEmail(
                specificRecipient,
                "指定收件人测试",
                "这封邮件发送到指定的收件人: " + specificRecipient
            );
            System.out.println("邮件已发送到: " + specificRecipient);
        } catch (Exception e) {
            System.err.println("发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
